//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import com.astronautlabs.mc.rezolve.RezolveMod;
//import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
//import com.astronautlabs.mc.rezolve.common.MachineEntity;
//import com.astronautlabs.mc.rezolve.databaseServer.DatabaseServerEntity;
//
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.entity.BlockEntityType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.entity.player.InventoryPlayer;
//import net.minecraft.world.Container;
//import net.minecraft.inventory.IContainerListener;
//import net.minecraft.inventory.IInventory;
//import net.minecraft.world.inventory.Slot;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.nbt.NBTTagByteArray;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.core.Direction;
//import net.minecraft.util.EnumHand;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.common.util.Constants.NBT;
//import net.minecraftforge.fml.common.SidedProxy;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//public class RemoteShellEntity extends MachineEntity implements ICableEndpoint, IContainerListener {
//	public static final String ID = "remote_shell";
//
//	private ArrayList<BlockPos> connectedMachines = null;
//
//	public RemoteShellEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
//		super(pType, pPos, pBlockState);
//		this.updateInterval = 10;
//	}
//
//	@Override
//	public CompoundTag writeToNBT(CompoundTag nbt) {
//
//		ListTag connectedMachinesList = new ListTag();
//
//		if (this.connectedMachines != null) {
//			for (BlockPos pos : this.connectedMachines) {
//
//				CompoundTag posTag = new CompoundTag();
//
//				posTag.putInt("X", pos.getX());
//				posTag.putInt("Y", pos.getY());
//				posTag.putInt("Z", pos.getZ());
//
//				connectedMachinesList.appendTag(posTag);
//			}
//
//			nbt.setTag("Machines", connectedMachinesList);
//		}
//
//		return super.writeToNBT(nbt);
//	}
//
//	@Override
//	public void readFromNBT(CompoundTag compound) {
//
//		if (compound.contains("Machines")) {
//			ListTag machineList = compound.getTagList("Machines", NBT.TAG_COMPOUND);
//
//			this.connectedMachines = new ArrayList<BlockPos>();
//			for (int i = 0, max = machineList.tagCount(); i < max; ++i) {
//				CompoundTag machineNBT = (CompoundTag)machineList.get(i);
//				this.connectedMachines.add(new BlockPos(machineNBT.getInt("X"), machineNBT.getInt("Y"), machineNBT.getInt("Z")));
//			}
//		} else {
//			this.connectedMachines = null;
//		}
//
//		super.readFromNBT(compound);
//	}
//
//	@Override
//	public void onCableUpdate() {
//		this.updateMachines();
//	}
//
//	public void updateMachinesIfNeeded() {
//		if (this.connectedMachines != null)
//			return;
//
//		this.updateMachines();
//	}
//
//	public void updateMachines() {
//		updateMachines(this.getPos());
//	}
//
//	public void updateMachines(BlockPos pos) {
//		CableNetwork network = new CableNetwork(this.worldObj, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
//		this.connectedMachines = new ArrayList<BlockPos>(Arrays.asList(network.getEndpoints()));
//
//		this.notifyUpdate();
//	}
//
//	@Override
//	public void update() {
//		super.update();
//
//		this.updateMachinesIfNeeded();
//	}
//
//	public BlockPos[] getConnectedMachines() {
//		if (this.connectedMachines == null)
//			return new BlockPos[0];
//
//		return this.connectedMachines.toArray(new BlockPos[this.connectedMachines.size()]);
//	}
//
//	ArrayList<Player> activatedPlayers = new ArrayList<Player>();
//	BlockPos clientActivatedMachine = null;
//
//	public BlockPos getClientActivatedMachine() {
//		return this.clientActivatedMachine;
//	}
//
//	@SidedProxy(
//			clientSide = "com.astronautlabs.mc.rezolve.remoteShell.RemoteShellClientProxy",
//			serverSide = "com.astronautlabs.mc.rezolve.remoteShell.RemoteShellServerProxy"
//	)
//	public static RemoteShellProxy proxy;
//
//	public void activate(BlockPos activatedMachine, Player player) {
//
//		if (this.storedEnergy < this.openEnergyCost) {
//			return;
//		}
//
//		if (this.getWorld().isRemote) {
//			RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellActivateMessage(this, activatedMachine, player.getUniqueID().toString()));
//			this.clientActivatedMachine = activatedMachine;
//
//			this.clientOverlay = proxy.addRemoteShellOverlay(this);
//			return;
//		}
//
//		this.storedEnergy -= this.openEnergyCost;
//		this.notifyUpdate();
//
//		BlockState state = this.getWorld().getBlockState(activatedMachine);
//
//		RezolveMod.setPlayerOverridePosition(player.getUniqueID(), activatedMachine);
//
//		System.out.println("Activating block using Remote Shell: "+state.getBlock().getRegistryName());
//
//		Container existingContainer = player.openContainer;
//
//		state.getBlock().onBlockActivated(
//				this.getWorld(), activatedMachine, state, player,
//				EnumHand.MAIN_HAND, null, Direction.NORTH,
//				0, 0, 0
//		);
//
//		if (existingContainer != player.openContainer) {
//			Container remoteContainer = player.openContainer;
//
//			// Open was successful.
//
//			synchronized (this.activatedPlayers) {
//				if (!this.activatedPlayers.contains(player))
//					this.activatedPlayers.add(player);
//			}
//
//			// Track events in the container
//
//			remoteContainer.addListener(this);
//		}
//
//	}
//
//	private int openEnergyCost = 1000;
//	private int constantDrawCost = 50;
//
//	private Object clientOverlay = null;
//	private GuiScreen currentGui = null;
//
//	@Override
//	protected void updatePeriodicallyOnClient() {
//		boolean guiChanged = false;
//		Player player = Minecraft.getInstance().thePlayer;
//
//		if (this.currentGui != Minecraft.getInstance().currentScreen) {
//			guiChanged = true;
//			this.currentGui = Minecraft.getInstance().currentScreen;
//		}
//
//		if (this.clientActivatedMachine == null)
//			return;
//
//		if (guiChanged) {
//			if (this.currentGui != null) {
//				System.out.println("GUI is now of type "+this.currentGui.getClass().getCanonicalName());
//			}
//
//			if (this.currentGui == null || this.currentGui instanceof RemoteShellScreen) {
//				this.clientActivatedMachine = null;
//				proxy.removeRemoteShellOverlay(this.clientOverlay);
//				this.clientOverlay = null;
//			}
//		}
//	}
//
//	@Override
//	public void updatePeriodically() {
//
//		ArrayList<Player> deactivatedPlayers = new ArrayList<Player>();
//
//		// Update based on players who have activated remote UIs
//
//		synchronized (activatedPlayers) {
//
//			for (Player player : this.activatedPlayers) {
//				if (player.openContainer == player.inventoryContainer || player.openContainer instanceof RemoteShellMenu) {
//					deactivatedPlayers.add(player);
//				} else {
//					// Take a constant power draw
//					if (this.storedEnergy < this.constantDrawCost) {
//						// Close that shit. Put some more power in there, cheapskate.
//						System.out.println("Closing UI due to lack of power.");
//
//						BlockState state = this.worldObj.getBlockState(this.getPos());
//						deactivatedPlayers.add(player);
//
//						player.closeScreen();
//
//						state.getBlock().onBlockActivated(
//							this.worldObj, this.getPos(), state, player, EnumHand.MAIN_HAND,
//							null, Direction.NORTH, 0, 0, 0)
//						;
//					} else {
//						this.storedEnergy -= this.constantDrawCost;
//						this.notifyUpdate();
//					}
//				}
//			}
//
//			for (Player player : deactivatedPlayers) {
//				System.out.println("Player has closed remote inventory, clearing override...");
//				RezolveMod.clearPlayerOverridePosition(player.getUniqueID());
//				this.activatedPlayers.remove(player);
//			}
//		}
//
//		// Update network configuration if needed (ie, a connected machine was destroyed)
//
//		boolean updateNeeded = false;
//
//		for (BlockPos pos : this.getConnectedMachines()) {
//			if (!RezolveMod.ETHERNET_CABLE_BLOCK.canConnectTo(this.getWorld(), pos)) {
//				updateNeeded = true;
//				break;
//			}
//		}
//
//		if (updateNeeded) {
//			System.out.println("An update to the ethernet network is required. Updating...");
//			this.updateMachines();
//		}
//	}
//
//	public void open(Player player) {
//
//		if (this.getWorld().isRemote)
//			return;
//
//		BlockState state = this.getWorld().getBlockState(this.getPos());
//		Block block = state.getBlock();
//
//		if (block instanceof RemoteShellBlock) {
//			((RemoteShellBlock)block).openGui(this.getWorld(), this.getPos(), player);
//		}
//	}
//
//	public void returnToShell() {
//		if (!this.getWorld().isRemote)
//			return;
//
//		this.clientActivatedMachine = null;
//		proxy.removeRemoteShellOverlay(this.clientOverlay);
//		this.clientOverlay = null;
//
//		Player player = Minecraft.getInstance().thePlayer;
//		RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellReturnMessage(this, player.getUniqueID().toString()));
//	}
//
//	public void returnToShell(Player player) {
//		if (this.getWorld().isRemote)
//			return;
//
//		if (!this.activatedPlayers.contains(player))
//			return;
//
//		this.activatedPlayers.remove(player);
//		this.open(player);
//	}
//
//	private int accessCharge = 10;
//
//	private void chargeForAccess(int itemCount) {
//
//		int totalCharge = this.accessCharge * itemCount;
//
//		if (this.storedEnergy < totalCharge) {
//			this.storedEnergy = 0;
//		} else {
//			this.storedEnergy -= this.accessCharge * itemCount;
//		}
//
//		// If we are out of energy, we'll let the tile entity update handle closing all connections.
//
//		this.notifyUpdate();
//	}
//
//	@Override
//	public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList) {
//		System.out.println("Send craft inventory:");
//		int index = 0;
//
//		for (ItemStack stack : itemsList) {
//			++index;
//
//			if (stack == null)
//				System.out.println("["+index+"] No item");
//			else
//				System.out.println("["+index+"] "+stack.getCount()+"x "+stack.getDisplayName());
//		}
//	}
//
//	public DatabaseServerEntity getDatabase()
//	{
//		for (BlockPos pos : this.connectedMachines) {
//			BlockEntity entity = this.getWorld().getBlockEntity(pos);
//
//			if (entity instanceof DatabaseServerEntity) {
//				return (DatabaseServerEntity)entity;
//			}
//		}
//
//		return null;
//	}
//
//	@Override
//	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
//
//		Slot slot = containerToSend.getSlot(slotInd);
//		boolean charge = true;
//
//		if (slot != null) {
//			if (slot.inventory instanceof InventoryPlayer) {
//				// We don't charge for changes to the player's inventory.
//				charge = false;
//			}
//		}
//
//		System.out.println("Send slot contents for "+slotInd+":");
//		if (stack == null)
//			System.out.println(" - No contents");
//		else
//			System.out.println(" - "+stack.getCount()+" "+stack.getDisplayName());
//
//		if (charge) {
//			System.out.println("Charging a fee for data transfer...");
//			this.chargeForAccess(1);
//		}
//
//	}
//
//	@Override
//	public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue) {
//		System.out.println("Send progress: "+varToUpdate+" :: "+newValue);
//
//	}
//
//	@Override
//	public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
//		System.out.println("Send all window props");
//
//	}
//
//	public void renameMachine(BlockPos machinePos, String name) {
//		if (this.getWorld().isRemote) {
//			System.out.println("Rename machine client side");
//			RezolvePacketHandler.INSTANCE.sendToServer(new RemoteShellRenameMachineMessage(this, machinePos, name));
//			return;
//		}
//		System.out.println("Rename machine server side");
//
//		DatabaseServerEntity dbServer = this.getDatabase();
//		if (dbServer == null)
//			return;
//
//		dbServer.setMachineName(machinePos, name);
//	}
//}
