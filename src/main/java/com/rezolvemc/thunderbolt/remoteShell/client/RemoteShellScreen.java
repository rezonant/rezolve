package com.rezolvemc.thunderbolt.remoteShell.client;

import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.ScreenFor;
import com.rezolvemc.thunderbolt.remoteShell.RemoteShellMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.Window;

@ScreenFor(RemoteShellMenu.class)
public class RemoteShellScreen extends MachineScreen<RemoteShellMenu> {
	public RemoteShellScreen(RemoteShellMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 255, 212);
		enableInventoryLabel = false;
	}

	@Override
	protected Window createMainWindow() {
		return new MachineListWindow();
	}
}
