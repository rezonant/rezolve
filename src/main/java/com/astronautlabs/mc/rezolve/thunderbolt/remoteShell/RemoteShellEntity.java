package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.thunderbolt.databaseServer.DatabaseServerEntity;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class RemoteShellEntity extends MachineEntity implements ICableEndpoint, ContainerListener {
	private ArrayList<BlockPos> connectedMachines = null;

	public RemoteShellEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(RemoteShellEntity.class), pPos, pBlockState);
		this.updateInterval = 10;
	}

	@Override
	public Component getMenuTitle() {
		return Component.literal("Remote Shell");
	}

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        ListTag connectedMachinesList = new ListTag();

        if (this.connectedMachines != null) {
            for (BlockPos pos : this.connectedMachines) {

                CompoundTag posTag = new CompoundTag();

                posTag.putInt("X", pos.getX());
                posTag.putInt("Y", pos.getY());
                posTag.putInt("Z", pos.getZ());

                connectedMachinesList.add(posTag);
            }

            tag.put("Machines", connectedMachinesList);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("Machines")) {
            ListTag machineList = tag.getList("Machines", Tag.TAG_COMPOUND);

            this.connectedMachines = new ArrayList<BlockPos>();
            for (int i = 0, max = machineList.size(); i < max; ++i) {
                CompoundTag machineNBT = (CompoundTag)machineList.get(i);
                this.connectedMachines.add(new BlockPos(machineNBT.getInt("X"), machineNBT.getInt("Y"), machineNBT.getInt("Z")));
            }
        } else {
            this.connectedMachines = null;
        }

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
		updateMachines(this.getBlockPos());
	}

	public void updateMachines(BlockPos pos) {
		CableNetwork network = new CableNetwork(this.level, pos, RezolveRegistry.block(EthernetCableBlock.class));
		this.connectedMachines = new ArrayList<BlockPos>(Arrays.asList(network.getEndpoints()));

		this.setChanged();
	}

	@Override
	public void tick() {
		super.tick();
		this.updateMachinesIfNeeded();
	}

	public BlockPos[] getConnectedMachines() {
		if (this.connectedMachines == null)
			return new BlockPos[0];

		return this.connectedMachines.toArray(new BlockPos[this.connectedMachines.size()]);
	}

	class PlayerActivationState {
		PlayerActivationState(Player player, boolean active, MachineListing activeMachine) {
			this.player = player;
			this.active = active;
			this.activeMachine = activeMachine;
		}

		public Player player;
		public boolean active;
		public MachineListing activeMachine;
	}

	List<PlayerActivationState> activatedPlayers = new ArrayList<>();
	BlockPos clientActivatedMachine = null;

	public BlockPos getClientActivatedMachine() {
		return this.clientActivatedMachine;
	}

	public void activate(BlockPos machinePos, Player player) {
		if (this.energy.extractEnergy(this.openEnergyCost, true) < this.openEnergyCost) {
			return;
		}

		this.energy.extractEnergy(this.openEnergyCost, false);

		BlockState machineBlockState = this.getLevel().getBlockState(machinePos);

		RezolveMod.setPlayerOverridePosition(player.getUUID(), machinePos);

		System.out.println("Activating block using Remote Shell: "+machineBlockState.getBlock().getName().toString());

		AbstractContainerMenu existingContainer = player.containerMenu;

		openBlock(player, machinePos);

		if (existingContainer != player.containerMenu) {
			var remoteContainer = player.containerMenu;

			// Open was successful.

			synchronized (this.activatedPlayers) {
				var activation = this.activatedPlayers.stream().filter(s -> s.player == player).findFirst().orElse(null);

				if (activation == null) {
					this.activatedPlayers.add(activation = new PlayerActivationState(player, true, getMachineAt(machinePos)));
				}

				activation.active = true;
				activation.activeMachine = new MachineListing(machinePos, "TODO", new ItemStack(machineBlockState.getBlock().asItem(), 1));
				sendPlayerState(activation);
			}

			// Track events in the container

			remoteContainer.addSlotListener(this);
		}

	}

	MachineListing getMachineAt(BlockPos pos) {
		return new MachineListing(pos, "TODO", new ItemStack(getBlockState().getBlock().asItem(), 1));
	}

	private void openBlock(Player player, BlockPos activatedMachine) {
		getLevel().getBlockState(activatedMachine).use(
			getLevel(), player, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(activatedMachine), Direction.UP, activatedMachine, false)
		);
	}

	private int openEnergyCost = 1000;
	private int constantDrawCost = 50;

	@Override
	public void updatePeriodically() {

		ArrayList<Player> deactivatedPlayers = new ArrayList<Player>();

		// Update based on players who have activated remote UIs

		synchronized (activatedPlayers) {

			for (PlayerActivationState state : this.activatedPlayers) {
				var player = state.player;
				if (player.containerMenu == player.inventoryMenu || player.containerMenu instanceof RemoteShellMenu) {
					deactivatedPlayers.add(player);
					state.active = false;
					state.activeMachine = null;
				} else {
					// Take a constant power draw
					if (this.getStoredEnergy() < this.constantDrawCost) {
						// Close that shit. Put some more power in there, cheapskate.
						System.out.println("Closing UI due to lack of power.");

						BlockState blockState = level.getBlockState(getBlockPos());
						deactivatedPlayers.add(player);

						player.closeContainer();
						openBlock(player, getBlockPos());

						state.active = false;
						state.activeMachine = null;
					} else {
						energy.extractEnergy(this.constantDrawCost, false);
						setChanged();
					}
				}

				sendPlayerState(state);
			}

			for (Player player : deactivatedPlayers) {
				System.out.println("Player has closed remote inventory, clearing override...");
				RezolveMod.clearPlayerOverridePosition(player.getUUID());
				this.activatedPlayers.remove(player);
			}
		}

		// Update network configuration if needed (ie, a connected machine was destroyed)

		boolean updateNeeded = false;

		for (BlockPos pos : this.getConnectedMachines()) {
			if (!RezolveRegistry.block(EthernetCableBlock.class).canConnectTo(this.getLevel(), pos)) {
				updateNeeded = true;
				break;
			}
		}

		if (updateNeeded) {
			System.out.println("An update to the ethernet network is required. Updating...");
			this.updateMachines();
		}
	}

	private void sendPlayerState(PlayerActivationState state) {
		var packet = new RemoteShellStatePacket();
		packet.remoteShellDimension = level.dimension().location().getPath();
		packet.remoteShellPosition = getBlockPos();
		packet.active = state.activeMachine != null;
		packet.activeMachine = state.activeMachine;
	}

	public void returnToShell(Player player) {
		if (this.getLevel().isClientSide)
			return;

		if (!this.activatedPlayers.contains(player))
			return;

		this.activatedPlayers.remove(player);

		NetworkHooks.openScreen(
			(ServerPlayer)player,
			player.level.getBlockState(getBlockPos()).getMenuProvider(player.level, getBlockPos())
		);
	}

	private int accessCharge = 10;

	private void chargeForAccess(int itemCount) {
		this.energy.extractEnergy(this.accessCharge * itemCount, false);

		// If we are out of energy, we'll let the tile entity update handle closing all connections.
		this.setChanged();
	}

	// TODO
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

	public DatabaseServerEntity getDatabase()
	{
		for (BlockPos pos : this.connectedMachines) {
			BlockEntity entity = this.getLevel().getBlockEntity(pos);

			if (entity instanceof DatabaseServerEntity) {
				return (DatabaseServerEntity)entity;
			}
		}

		return null;
	}


	@Override
	public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

	}

	@Override
	public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
		Slot slot = containerToSend.getSlot(dataSlotIndex);
		boolean charge = true;

		if (slot != null) {
			if (slot.container instanceof Inventory) {
				// We don't charge for changes to the player's inventory.
				charge = false;
			}
		}

		System.out.println("Send slot contents for "+dataSlotIndex+":");
		if (stack == null)
			System.out.println(" - No contents");
		else
			System.out.println(" - "+stack.getCount()+" "+stack.getDisplayName());

		if (charge) {
			System.out.println("Charging a fee for data transfer...");
			this.chargeForAccess(1);
		}

	}

	public void renameMachine(BlockPos machinePos, String name) {
		System.out.println("Rename machine server side");

		DatabaseServerEntity dbServer = this.getDatabase();
		if (dbServer == null)
			return;

		dbServer.setMachineName(machinePos, name);
	}
}
