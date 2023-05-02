package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

public class RemoteShellMenu extends MachineMenu<RemoteShellEntity> {
	public RemoteShellMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public RemoteShellMenu(int containerId, Inventory playerInv, RemoteShellEntity entity) {
		super(RezolveRegistry.menuType(RemoteShellMenu.class), containerId, playerInv, entity);

		if (entity != null)
			entity.startPlayerSession((ServerPlayer) playerInv.player);
	}
}
