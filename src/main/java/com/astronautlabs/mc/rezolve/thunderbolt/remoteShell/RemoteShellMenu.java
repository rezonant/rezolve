package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.machines.Sync;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.network.WithPacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets.RemoteShellActivatePacket;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets.RemoteShellMenuReturnPacket;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets.RemoteShellRenameMachinePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@RegistryId("remote_shell")
@WithScreen(RemoteShellScreen.class)
@WithPacket(RemoteShellActivatePacket.class)
public class RemoteShellMenu extends MachineMenu<RemoteShellEntity> {
	public RemoteShellMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public RemoteShellMenu(int containerId, Inventory playerInv, RemoteShellEntity entity) {
		super(RezolveRegistry.menuType(RemoteShellMenu.class), containerId, playerInv, entity);
		updateSearchResults();
	}

	@Sync public String searchQuery = "";
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
			var name = machineBlockState.getBlock().getName().getString();
			if (db != null)
				name = db.getMachineName(machinePos);
			var item = new ItemStack(machineBlockState.getBlock().asItem(), 1);

			searchResults.machines.add(new MachineListing(endpoint.getLevelKey(), machinePos, name, item));
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
	public void receivePacketOnServer(RezolvePacket rezolvePacket) {
		if (rezolvePacket instanceof RemoteShellActivatePacket activation) {
			machine.activate(activation.getLevel(), activation.getActivatedMachine(), machine.getLevel().getPlayerByUUID(UUID.fromString(activation.getPlayerId())));
		} else if (rezolvePacket instanceof RemoteShellRenameMachinePacket rename) {
			machine.renameMachine(rename.getMachinePos(), rename.getName());
		} else if (rezolvePacket instanceof RemoteShellMenuReturnPacket ret) {
			machine.returnToShell(machine.getLevel().getPlayerByUUID(UUID.fromString(ret.getPlayerId())));
		} else {
			super.receivePacketOnServer(rezolvePacket);
		}
	}
}
