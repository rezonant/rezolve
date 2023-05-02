package com.rezolvemc.thunderbolt.remoteShell;

import java.util.*;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.thunderbolt.cable.CableNetwork;
import com.rezolvemc.thunderbolt.databaseServer.DatabaseServerEntity;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellRenameMachinePacket;
import com.rezolvemc.thunderbolt.remoteShell.server.RemoteAccessEndpoint;
import com.rezolvemc.thunderbolt.remoteShell.server.RemoteAccessSession;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	public CableNetwork.Endpoint[] getConnectedMachines() {
		List<CableNetwork.Endpoint> machines = new ArrayList<>();

		for (var network : getNetworks()) {
			machines.addAll(List.of(network.getEndpoints()));
		}

		machines = machines.stream().filter(m -> !m.equals(getBlockPos())).toList();

		return machines.toArray(new CableNetwork.Endpoint[machines.size()]);
	}

	private RemoteAccessEndpoint remoteAccessEndpoint = new Endpoint();

	public void startPlayerSession(ServerPlayer player) {
		remoteAccessEndpoint.startSession(player);
	}

	@Override
	public void updatePeriodically() {
		remoteAccessEndpoint.updatePeriodically();
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
		if (rezolvePacket instanceof RemoteShellRenameMachinePacket rename) {
			renameMachine(rename.getMachinePos(), rename.getName());
			return;
		}

		if (remoteAccessEndpoint.handlePacket(rezolvePacket, player))
			return;

		super.receivePacketOnServer(rezolvePacket, player);
	}

	private class Endpoint extends RemoteAccessEndpoint {
		public Endpoint() {
			super(RemoteShellEntity.this);
		}

		@Override
		public Level getLevel() {
			return RemoteShellEntity.this.getLevel();
		}

		@Override
		public BlockPos getBlockPos() {
			return RemoteShellEntity.this.getBlockPos();
		}

		@Override
		public int getEnergyCapacity() {
			return RemoteShellEntity.this.getEnergyCapacity();
		}

		@Override
		public int getStoredEnergy() {
			return RemoteShellEntity.this.getStoredEnergy();
		}

		@Override
		public List<MachineListing> getConnectedMachines() {
			var db = getDatabase();
			var list = new ArrayList<MachineListing>();

			for (var endpoint : RemoteShellEntity.this.getConnectedMachines()) {
				var machinePos = endpoint.getPosition();
				var machineBlockState = endpoint.getBlockState();
				var name = machineBlockState.getBlock().getName();
				if (db != null)
					name = Component.literal(db.getMachineName(machinePos));
				var item = new ItemStack(machineBlockState.getBlock().asItem(), 1);

				list.add(new MachineListing(endpoint.getLevelKey(), machinePos, null, item));
			}

			return list;
		}

		@Override
		public int expendEnergy(int amount, boolean simulate) {
			int spent = RemoteShellEntity.this.energy.extractEnergy(amount, simulate);
			RemoteShellEntity.this.setChanged();
			return spent;
		}

		@Override
		public boolean isValidDestination(LevelPosition position) {
			CableNetwork.Endpoint machineEndpoint = null;

			for (var network : getNetworks()) {
				var potentialEndpoint = network.getEndpoint(position.getLevelKey(), position.getPosition());
				if (potentialEndpoint != null) {
					machineEndpoint = potentialEndpoint;
					break;
				}
			}

			if (machineEndpoint == null)
				return false;

			return true;
		}

		@Override
		protected void afterDisconnect(RemoteAccessSession session, MachineListing machine) {
			if (session.activeMachine != null) {
				var level = getLevel().getServer().getLevel(session.activeMachine.getLevel());
				var chunkPos = new ChunkPos(session.activeMachine.getBlockPos());
				ForgeChunkManager.forceChunk(level, Rezolve.ID, session.player, chunkPos.x, chunkPos.z, false, false);
			}
		}

		@Override
		protected boolean stillValid(RemoteAccessSession session) {
			if (session.player.containerMenu instanceof RemoteShellMenu remoteShellMenu) {
				if (remoteShellMenu.getMachine() != RemoteShellEntity.this) {
					return false;
				}
			}

			return super.stillValid(session);
		}
	}
}
