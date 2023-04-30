package com.rezolvemc.thunderbolt.remoteShell;

import java.util.*;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.thunderbolt.cable.CableNetwork;
import com.rezolvemc.thunderbolt.databaseServer.DatabaseServerEntity;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellEntityReturnPacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStartRecordingPacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStopRecordingPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class RemoteShellEntity extends MachineEntity {
	private static final Logger LOGGER = LogManager.getLogger(Rezolve.ID);

	public RemoteShellEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(RemoteShellEntity.class), pPos, pBlockState);
		this.updateInterval = 10;
	}

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.remote_shell");
	}

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        ListTag connectedMachinesList = new ListTag();

		// TODO
//        if (this.connectedMachines != null) {
//            for (BlockPos pos : this.connectedMachines) {
//
//                CompoundTag posTag = new CompoundTag();
//
//                posTag.putInt("X", pos.getX());
//                posTag.putInt("Y", pos.getY());
//                posTag.putInt("Z", pos.getZ());
//
//                connectedMachinesList.add(posTag);
//            }
//
//            tag.put("Machines", connectedMachinesList);
//        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

//        if (tag.contains("Machines")) {
//            ListTag machineList = tag.getList("Machines", Tag.TAG_COMPOUND);
//
//            this.connectedMachines = new ArrayList<BlockPos>();
//            for (int i = 0, max = machineList.size(); i < max; ++i) {
//                CompoundTag machineNBT = (CompoundTag)machineList.get(i);
//                this.connectedMachines.add(new BlockPos(machineNBT.getInt("X"), machineNBT.getInt("Y"), machineNBT.getInt("Z")));
//            }
//        } else {
//            this.connectedMachines = null;
//        }
    }

	public CableNetwork.Endpoint[] getConnectedMachines() {
		List<CableNetwork.Endpoint> machines = new ArrayList<>();

		for (var network : getNetworks()) {
			machines.addAll(List.of(network.getEndpoints()));
		}

		machines = machines.stream().filter(m -> !m.equals(getBlockPos())).toList();

		return machines.toArray(new CableNetwork.Endpoint[machines.size()]);
	}

	class PlayerActivationState {
		PlayerActivationState(Player player, boolean active, @Nonnull MachineListing activeMachine) {
			this.player = player;
			this.active = active;
			this.activeMachine = activeMachine;
		}

		public Player player;
		public boolean active;
		public MachineListing activeMachine;

		private boolean recording;
		private List<RecordedAction> recordedActions;

		public boolean isRecording() {
			return recording;
		}

		private record RecordedAction(ResourceKey<Level> level, BlockPos block, int slot, Action action, ItemStack items) {}

		public void startRecording() {
			recording = true;
			recordedActions = new ArrayList<>();
		}

		public void record(ResourceKey<Level> level, BlockPos block, int slot, Action action, ItemStack items) {
			recordedActions.add(new RecordedAction(level, block, slot, action, items));
		}

		public RecordedAction[] finishRecording() {
			recording = false;
			var set = recordedActions.toArray(new RecordedAction[recordedActions.size()]);
			recordedActions = null;
			return set;
		}

		public enum Action {
			INSERT,
			EXTRACT
		}
	}

	List<PlayerActivationState> activatedPlayers = new ArrayList<>();

	private MinecraftServer getServer() {
		return getLevel().getServer();
	}

	public void activate(ResourceKey<Level> levelKey, BlockPos machinePos, Player player) {
		if (this.energy.extractEnergy(this.openEnergyCost, true) < this.openEnergyCost) {
			return;
		}

		// Ensure that the passed location is actually on an attached cable network before proceeding.
		CableNetwork.Endpoint endpoint = null;

		for (var network : getNetworks()) {
			var potentialEndpoint = network.getEndpoint(levelKey, machinePos);
			if (potentialEndpoint != null) {
				endpoint = potentialEndpoint;
				break;
			}
		}

		if (endpoint == null)
			return;

		this.energy.extractEnergy(this.openEnergyCost, false);

		// We add an override entry for this player to the endpoint location (in the specific level)
		// so that Rezolve's mixin can do container stillValid() checks against the simulated location,
		// so that any checks not specifically related to player location will still function as expected.

		Rezolve.setPlayerOverridePosition(
				player.getUUID(),
				endpoint.getLevelPosition()
		);

		System.out.println("Activating block using Remote Shell: "+endpoint.getBlock().getName().toString());

		AbstractContainerMenu existingContainer = player.containerMenu;

		var chunkPos = new ChunkPos(endpoint.getPosition());

		ForgeChunkManager.forceChunk((ServerLevel)endpoint.getLevel(), Rezolve.ID, player, chunkPos.x, chunkPos.z, true, true);
		openBlock(player, endpoint.getLevel(), endpoint.getPosition());

		if (existingContainer != player.containerMenu) {
			var remoteContainer = player.containerMenu;

			// Open was successful.

			synchronized (this.activatedPlayers) {
				var activation = getPlayerState(player);
				var machineListing = new MachineListing(
						endpoint.getLevelKey(),
						machinePos,
						null, // TODO
						new ItemStack(endpoint.getBlockItem(), 1)
				);

				if (activation == null) {
					this.activatedPlayers.add(activation = new PlayerActivationState(player, true, machineListing));
				}

				activation.active = true;
				activation.activeMachine = machineListing;

				sendPlayerState(activation);
			}

			// Track events in the container

			remoteContainer.addSlotListener(new ContainerListener() {
				Map<Integer, ItemStack> itemState = new HashMap<>();
				ItemStack carried = remoteContainer.getCarried();

				{
					for (int i = 0, max = remoteContainer.slots.size(); i < max; ++i) {
						itemState.put(i, remoteContainer.getSlot(i).getItem());
					}
				}

				@Override
				public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {

					ItemStack oldItem = itemState.get(dataSlotIndex);
					ItemStack oldCarried = carried;

					itemState.put(dataSlotIndex, stack);
					carried = remoteContainer.getCarried();

					Slot slot = containerToSend.getSlot(dataSlotIndex);
					boolean charge = true;

					if (slot != null) {
						if (slot.container instanceof Inventory) {
							// We don't charge for changes to the player's inventory.
							charge = false;
						}
					}

					ItemStack netStack = null;
					PlayerActivationState.Action action = null;
					if (oldItem.sameItem(stack)) {
						netStack = new ItemStack(oldItem.getItem(), Math.abs(stack.getCount() - oldItem.getCount()));
					} else if (!oldItem.isEmpty()) {
						netStack = oldItem.copy();
						action = PlayerActivationState.Action.EXTRACT;
					} else if (!stack.isEmpty()) {
						netStack = stack.copy();
						action = PlayerActivationState.Action.INSERT;
					}

//					System.out.println("[Slot] "+dataSlotIndex+": "+ oldItem.toString() + " -> " + stack.toString());
//					System.out.println("[Carried] "+dataSlotIndex+": "+ oldCarried.toString() + " -> " + carried.toString());

					if (charge && netStack != null) {
						chargeForAccess(netStack);

						var activation = getPlayerState(player);
						if (activation != null && activation.isRecording() && action != null) {
							activation.record(levelKey, machinePos, dataSlotIndex, action, netStack);
						}
					}

				}

				@Override
				public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

				}
			});
		}

	}

	private void openBlock(Player player, Level level, BlockPos pos) {
		level.getBlockState(pos).use(
			level, player, InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)
		);
	}

	private int openEnergyCost = 1000;
	private int constantDrawCost = 50;

	@Override
	public void updatePeriodically() {

		ArrayList<PlayerActivationState> deactivatedPlayers = new ArrayList<>();

		// Update based on players who have activated remote UIs

		synchronized (activatedPlayers) {

			for (PlayerActivationState state : this.activatedPlayers) {
				var player = state.player;
				if (player.containerMenu == player.inventoryMenu || player.containerMenu instanceof RemoteShellMenu) {
					deactivatedPlayers.add(state);
				} else {
					// Take a constant power draw
					if (this.getStoredEnergy() < this.constantDrawCost) {
						// Close that shit. Put some more power in there, cheapskate.
						System.out.println("Closing UI due to lack of power.");

						BlockState blockState = level.getBlockState(getBlockPos());
						deactivatedPlayers.add(state);

						player.closeContainer();
						openBlock(player, getLevel(), getBlockPos());
					} else {
						energy.extractEnergy(this.constantDrawCost, false);
						setChanged();
					}
				}

				sendPlayerState(state);
			}

			for (var activationState : deactivatedPlayers) {
				System.out.println("Player has closed remote inventory, clearing override...");
				deactivatePlayer(activationState);
			}
		}
	}

	private void deactivatePlayer(PlayerActivationState activationState) {
		var player = activationState.player;
		Rezolve.clearPlayerOverridePosition(player.getUUID());

		var level = getServer().getLevel(activationState.activeMachine.getLevel());
		var chunkPos = new ChunkPos(activationState.activeMachine.getBlockPos());

		ForgeChunkManager.forceChunk(level, Rezolve.ID, activationState.player, chunkPos.x, chunkPos.z, false, false);

		activationState.active = false;
		activationState.activeMachine = null;

		sendPlayerState(activationState);
		this.activatedPlayers.remove(activationState);
	}

	private void sendPlayerState(PlayerActivationState state) {
		var packet = new RemoteShellStatePacket();
		packet.remoteShellDimension = level.dimension().location().getPath();
		packet.remoteShellPosition = getBlockPos();
		packet.active = state.activeMachine != null;
		packet.activeMachine = state.activeMachine;
		packet.remoteShellEnergy = getStoredEnergy();
		packet.remoteShellEnergyCapacity = getEnergyCapacity();
		packet.sendToPlayer((ServerPlayer) state.player);
	}

	public PlayerActivationState getPlayerState(Player player) {
		if (player == null)
			return null;
		return this.activatedPlayers.stream().filter(s -> Objects.equals(s.player.getStringUUID(), player.getStringUUID())).findFirst().orElse(null);
	}

	public void startRecording(Player player) {
		if (this.getLevel().isClientSide)
			return;

		var state = getPlayerState(player);
		if (state == null)
			return;

		state.startRecording();
	}

	public void stopRecording(Player player) {
		if (this.getLevel().isClientSide)
			return;

		var state = getPlayerState(player);
		if (state == null)
			return;

		var actions = state.finishRecording();
		LOGGER.info("Would make the pattern!");
		// TODO
	}

	public void returnToShell(Player player) {
		if (this.getLevel().isClientSide)
			return;

		var state = getPlayerState(player);

		if (state == null)
			return;

		deactivatePlayer(state);

		NetworkHooks.openScreen(
			(ServerPlayer)player,
			player.level.getBlockState(getBlockPos()).getMenuProvider(player.level, getBlockPos())
		);
	}

	private int accessCharge = 10;

	private void chargeForAccess(ItemStack items) {
		int charge = this.accessCharge * items.getCount();
		//System.out.println("** Charging " + charge + " FE for transferring "+items);

		this.energy.extractEnergy(charge, false);

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
		if (getNetwork() == null)
			return null;

		return getNetwork().getDatabaseServer();
	}

	public void renameMachine(BlockPos machinePos, String name) {
		System.out.println("Rename machine server side");

		DatabaseServerEntity dbServer = this.getDatabase();
		if (dbServer == null)
			return;

		dbServer.setMachineName(machinePos, name);
	}

	@Override
	public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
		if (rezolvePacket instanceof RemoteShellEntityReturnPacket returnPacket) {
			returnToShell(player);
		} else if (rezolvePacket instanceof RemoteShellStartRecordingPacket) {
			startRecording(player);
		} else if (rezolvePacket instanceof RemoteShellStopRecordingPacket) {
			stopRecording(player);
		} else {
			super.receivePacketOnServer(rezolvePacket, player);
		}
	}
}
