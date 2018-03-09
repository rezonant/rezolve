package com.astronautlabs.mc.rezolve.remoteShell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.cable.CableNetwork;
import com.astronautlabs.mc.rezolve.cable.ICableEndpoint;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.databaseServer.DatabaseServerEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.SidedProxy;

public class RemoteShellEntity extends MachineEntity implements ICableEndpoint, IContainerListener {
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
		CableNetwork network = new CableNetwork(this.worldObj, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
		this.connectedMachines = new ArrayList<BlockPos>(Arrays.asList(network.getEndpoints()));
		
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
	BlockPos clientActivatedMachine = null;
	
	public BlockPos getClientActivatedMachine() {
		return this.clientActivatedMachine;
	}

	@SidedProxy(
			clientSide = "com.astronautlabs.mc.rezolve.remoteShell.RemoteShellClientProxy", 
			serverSide = "com.astronautlabs.mc.rezolve.remoteShell.RemoteShellServerProxy"
	)
	public static RemoteShellProxy proxy;

	public void activate(BlockPos activatedMachine, EntityPlayer player) {
		
		if (this.storedEnergy < this.openEnergyCost) {
			return;
		}

		if (this.getWorld().isRemote) {
			RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellActivateMessage(this, activatedMachine, player.getUniqueID().toString()));
			this.clientActivatedMachine = activatedMachine;

			this.clientOverlay = proxy.addRemoteShellOverlay(this);
			return;
		}
		
		this.storedEnergy -= this.openEnergyCost;
		this.notifyUpdate();
		
		IBlockState state = this.getWorld().getBlockState(activatedMachine);

		RezolveMod.setPlayerOverridePosition(player.getUniqueID(), activatedMachine);

		System.out.println("Activating block using Remote Shell: "+state.getBlock().getRegistryName());
		
		Container existingContainer = player.openContainer;
		
		state.getBlock().onBlockActivated(
				this.getWorld(), activatedMachine, state, player, 
				EnumHand.MAIN_HAND, null, EnumFacing.NORTH, 
				0, 0, 0
		);

		if (existingContainer != player.openContainer) {
			Container remoteContainer = player.openContainer;
	
			// Open was successful.

			synchronized (this.activatedPlayers) {
				if (!this.activatedPlayers.contains(player))
					this.activatedPlayers.add(player);	
			}
			
			// Track events in the container 
			
			remoteContainer.addListener(this);
		}
		
	}
	
	private int openEnergyCost = 1000;
	private int constantDrawCost = 50;
	
	private Object clientOverlay = null;
	private GuiScreen currentGui = null;
	
	@Override
	protected void updatePeriodicallyOnClient() {
		boolean guiChanged = false;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		if (this.currentGui != Minecraft.getMinecraft().currentScreen) {
			guiChanged = true;
			this.currentGui = Minecraft.getMinecraft().currentScreen;
		}
		
		if (this.clientActivatedMachine == null)
			return;
		
		if (guiChanged) {
			if (this.currentGui != null) {
				System.out.println("GUI is now of type "+this.currentGui.getClass().getCanonicalName());
			}
			
			if (this.currentGui == null || this.currentGui instanceof RemoteShellGuiContainer) {
				this.clientActivatedMachine = null;
				proxy.removeRemoteShellOverlay(this.clientOverlay);
				this.clientOverlay = null;
			}
		}
	}
	
	@Override
	public void updatePeriodically() {
		
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

	public void open(EntityPlayer player) {
		
		if (this.getWorld().isRemote)
			return;
		
		IBlockState state = this.getWorld().getBlockState(this.getPos());
		Block block = state.getBlock();
		
		if (block instanceof RemoteShellBlock) {
			((RemoteShellBlock)block).openGui(this.getWorld(), this.getPos(), player);
		}
	}

	public void returnToShell() {
		if (!this.getWorld().isRemote)
			return;

		this.clientActivatedMachine = null;
		proxy.removeRemoteShellOverlay(this.clientOverlay);
		this.clientOverlay = null;
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellReturnMessage(this, player.getUniqueID().toString()));
	}
	
	public void returnToShell(EntityPlayer player) {
		if (this.getWorld().isRemote)
			return;
		
		if (!this.activatedPlayers.contains(player))
			return;
		
		this.activatedPlayers.remove(player);
		this.open(player);
	}
	
	private int accessCharge = 10;
	
	private void chargeForAccess(int itemCount) {
		
		int totalCharge = this.accessCharge * itemCount;
		
		if (this.storedEnergy < totalCharge) {
			this.storedEnergy = 0;
		} else {
			this.storedEnergy -= this.accessCharge * itemCount;
		}
		
		// If we are out of energy, we'll let the tile entity update handle closing all connections.
		
		this.notifyUpdate();
	}

	@Override
	public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList) {
		System.out.println("Send craft inventory:");
		int index = 0;
		
		for (ItemStack stack : itemsList) {
			++index;
			
			if (stack == null)
				System.out.println("["+index+"] No item");
			else
				System.out.println("["+index+"] "+stack.stackSize+"x "+stack.getDisplayName());
		}
	}

	public DatabaseServerEntity getDatabase()
	{
		for (BlockPos pos : this.connectedMachines) {
			TileEntity entity = this.getWorld().getTileEntity(pos);
			
			if (entity instanceof DatabaseServerEntity) {
				return (DatabaseServerEntity)entity;
			}
		}
		
		return null;
	}
	
	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		
		Slot slot = containerToSend.getSlot(slotInd);
		boolean charge = true;
		
		if (slot != null) {
			if (slot.inventory instanceof InventoryPlayer) {
				// We don't charge for changes to the player's inventory.
				charge = false;
			}
		}
		
		System.out.println("Send slot contents for "+slotInd+":");
		if (stack == null)
			System.out.println(" - No contents");
		else
			System.out.println(" - "+stack.stackSize+" "+stack.getDisplayName());
		
		if (charge) {
			System.out.println("Charging a fee for data transfer...");
			this.chargeForAccess(1);
		}
		
	}

	@Override
	public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue) {
		System.out.println("Send progress: "+varToUpdate+" :: "+newValue);
		
	}

	@Override
	public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
		System.out.println("Send all window props");
		
	}

	public void renameMachine(BlockPos machinePos, String name) {
		if (this.getWorld().isRemote) {
			System.out.println("Rename machine client side");
			RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellRenameMachineMessage(this, machinePos, name));
			return;
		}
		System.out.println("Rename machine server side");
		
		DatabaseServerEntity dbServer = this.getDatabase();
		if (dbServer == null)
			return;
		
		dbServer.setMachineName(machinePos, name);
	}
}
