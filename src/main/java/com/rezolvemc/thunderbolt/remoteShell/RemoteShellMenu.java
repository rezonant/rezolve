package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.common.registry.WithScreen;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.Sync;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.network.WithPacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellActivatePacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellMenuReturnPacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellRenameMachinePacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@RegistryId("remote_shell")
@WithScreen(RemoteShellScreen.class)
@WithPacket(RemoteShellActivatePacket.class)
@WithPacket(RemoteShellStatePacket.class)
public class RemoteShellMenu extends MachineMenu<RemoteShellEntity> {
	public RemoteShellMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public RemoteShellMenu(int containerId, Inventory playerInv, RemoteShellEntity entity) {
		super(RezolveRegistry.menuType(RemoteShellMenu.class), containerId, playerInv, entity);
		updateSearchResults();
	}

	@Sync
    public String searchQuery = "";
	@Sync public boolean hasDatabase = false;
	@Sync public MachineListingSearchResults searchResults = new MachineListingSearchResults();

	@Override
	protected void updateState() {
		super.updateState();
		hasDatabase = machine.getDatabase() != null;
	}

	public void activate(MachineListing listing, Player player) {
		var packet = new RemoteShellActivatePacket(listing.getLevel(), listing.getBlockPos(), player.getStringUUID());
		packet.setMenu(this);
		packet.sendToServer();
	}

	public void setSearchQuery(String query) {
		this.searchQuery = query;
		this.updateSearchResults();
	}

	private void updateSearchResults() {
		if (this.machine == null)
			return; // client side call

		var db = this.machine.getDatabase();
		searchResults.machines.clear();

		for (var endpoint : this.machine.getConnectedMachines()) {
			var machinePos = endpoint.getPosition();
			var machineBlockState = endpoint.getBlockState();
			var name = machineBlockState.getBlock().getName();
			if (db != null)
				name = Component.literal(db.getMachineName(machinePos));
			var item = new ItemStack(machineBlockState.getBlock().asItem(), 1);

			searchResults.machines.add(new MachineListing(endpoint.getLevelKey(), machinePos, null, item));
		}
	}

	public void renameMachine(BlockPos machine, String name) {
		var packet = new RemoteShellRenameMachinePacket(machine, name);
		packet.setMenu(this);
		packet.sendToServer();
	}

	public void returnToShell(Player player) {
		var packet = new RemoteShellMenuReturnPacket(player.getStringUUID());
		packet.setMenu(this);
		packet.sendToServer();
	}

	@Override
	public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
		if (rezolvePacket instanceof RemoteShellActivatePacket activation) {
			machine.activate(activation.getLevel(), activation.getActivatedMachine(), machine.getLevel().getPlayerByUUID(UUID.fromString(activation.getPlayerId())));
		} else if (rezolvePacket instanceof RemoteShellRenameMachinePacket rename) {
			machine.renameMachine(rename.getMachinePos(), rename.getName());
		} else if (rezolvePacket instanceof RemoteShellMenuReturnPacket ret) {
			machine.returnToShell(machine.getLevel().getPlayerByUUID(UUID.fromString(ret.getPlayerId())));
		} else {
			super.receivePacketOnServer(rezolvePacket, player);
		}
	}
}
