package com.astronautlabs.mc.rezolve.remoteShell;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.MachineEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import scala.actors.threadpool.Arrays;

public class RemoteShellEntity extends MachineEntity implements ICableEndpoint {
	public RemoteShellEntity() {
		super("remote_shell_entity");
		
		this.updateInterval = 10;
	}
	
	private ArrayList<BlockPos> connectedMachines = null;
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		
		NBTTagList connectedMachinesList = new NBTTagList();
		
		if (this.connectedMachines != null) {
			for (BlockPos pos : this.connectedMachines) {
				
				NBTTagCompound posTag = new NBTTagCompound();
	
				posTag.setInteger("X", pos.getX());
				posTag.setInteger("Y", pos.getY());
				posTag.setInteger("Z", pos.getZ());
				
				connectedMachinesList.appendTag(posTag);
			}
			
			nbt.setTag("Machines", connectedMachinesList);
		}
		
		return super.writeToNBT(nbt);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		if (compound.hasKey("Machines")) {
			NBTTagList machineList = compound.getTagList("Machines", NBT.TAG_COMPOUND);
			
			this.connectedMachines = new ArrayList<BlockPos>();
			for (int i = 0, max = machineList.tagCount(); i < max; ++i) {
				NBTTagCompound machineNBT = (NBTTagCompound)machineList.get(i);
				this.connectedMachines.add(new BlockPos(machineNBT.getInteger("X"), machineNBT.getInteger("Y"), machineNBT.getInteger("Z")));
			}
		} else {
			this.connectedMachines = null;
		}
		
		super.readFromNBT(compound);
	}

	@Override
	public void onCableUpdate() {
		System.out.println("Remote shell was notified that a connected cable network was changed. Updating machines...");
		this.updateMachines();
	}
	
	public void updateMachinesIfNeeded() {
		if (this.connectedMachines != null)
			return;
		
		this.updateMachines();
	}
	
	public void updateMachines() {
		updateMachines(this.getPos());
	}
	
	public void updateMachines(BlockPos pos) {
		System.out.println("Scanning for connected machines...");
		CableNetwork network = new CableNetwork(this.worldObj, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
		this.connectedMachines = new ArrayList<BlockPos>(Arrays.asList(network.getEndpoints()));
		
		for (BlockPos mac : this.connectedMachines) {
			System.out.println(" - Connected at "+mac.toString());
		}
		
		this.notifyUpdate();
	}
	
	@Override
	public void update() {
		super.update();
		
		this.updateMachinesIfNeeded();
	}
	
	public BlockPos[] getConnectedMachines() {
		if (this.connectedMachines == null)
			return new BlockPos[0];
		
		return this.connectedMachines.toArray(new BlockPos[this.connectedMachines.size()]);
	}

	ArrayList<EntityPlayer> activatedPlayers = new ArrayList<EntityPlayer>();
	
	public void activate(BlockPos activatedMachine, EntityPlayer player) {
		if (this.getWorld().isRemote) {
			RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellActivateMessage(this, activatedMachine, player.getUniqueID().toString()));
			return;
		}
		
		if (this.storedEnergy < this.openEnergyCost) {
			return;
		}
		
		this.storedEnergy -= this.openEnergyCost;
		this.notifyUpdate();
		
		IBlockState state = this.getWorld().getBlockState(activatedMachine);

		System.out.println("Activating block using Remote Shell: "+state.getBlock().getRegistryName());
		
		state.getBlock().onBlockActivated(this.getWorld(), activatedMachine, state, player, EnumHand.MAIN_HAND, null, EnumFacing.NORTH, 0, 0, 0);

		System.out.println("Overriding player position...");
		RezolveMod.setPlayerOverridePosition(player.getUniqueID(), activatedMachine);
		
		synchronized (this.activatedPlayers) {
			if (!this.activatedPlayers.contains(player))
				this.activatedPlayers.add(player);	
		}
	}
	
	private int openEnergyCost = 1000;
	private int constantDrawCost = 50;
	
	@Override
	public void updatePeriodically() {
		
		if (this.getWorld().isRemote)
			return;
		
		ArrayList<EntityPlayer> deactivatedPlayers = new ArrayList<EntityPlayer>();
		
		// Update based on players who have activated remote UIs
		
		synchronized (activatedPlayers) {
			
			for (EntityPlayer player : this.activatedPlayers) {
				if (player.openContainer == player.inventoryContainer || player.openContainer instanceof RemoteShellContainer) {
					deactivatedPlayers.add(player);
				} else {
					// Take a constant power draw
					if (this.storedEnergy < this.constantDrawCost) {
						// Close that shit. Put some more power in there, cheapskate.
						System.out.println("Closing UI due to lack of power.");
						
						IBlockState state = this.worldObj.getBlockState(this.getPos());
						deactivatedPlayers.add(player);
						
						player.closeScreen();
						
						state.getBlock().onBlockActivated(
							this.worldObj, this.getPos(), state, player, EnumHand.MAIN_HAND, 
							null, EnumFacing.NORTH, 0, 0, 0)
						;
					} else {
						this.storedEnergy -= this.constantDrawCost;
						System.out.println("Drew "+this.constantDrawCost+" RF to sustain remote connection. "+this.storedEnergy+" RF remains.");
						
						this.notifyUpdate();
					}
				}
			}
			
			for (EntityPlayer player : deactivatedPlayers) {
				System.out.println("Player has closed remote inventory, clearing override...");
				RezolveMod.clearPlayerOverridePosition(player.getUniqueID());
				this.activatedPlayers.remove(player);
			}
		}
		
		// Update network configuration if needed (ie, a connected machine was destroyed)
		
		boolean updateNeeded = false;
		
		for (BlockPos pos : this.getConnectedMachines()) {
			if (!RezolveMod.ETHERNET_CABLE_BLOCK.canConnectTo(this.getWorld(), pos)) {
				updateNeeded = true;
				break;
			}
		}
		
		if (updateNeeded) {
			System.out.println("An update to the ethernet network is required. Updating...");
			this.updateMachines();
		}
	}
}
