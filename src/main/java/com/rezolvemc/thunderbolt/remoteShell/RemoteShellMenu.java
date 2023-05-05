package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.common.machines.MachineMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

public class RemoteShellMenu extends MachineMenu<RemoteShellEntity> {
	public RemoteShellMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public RemoteShellMenu(int containerId, Inventory playerInv, RemoteShellEntity entity) {
		super(containerId, playerInv, entity);

		if (entity != null)
			entity.startPlayerSession((ServerPlayer) playerInv.player);
	}
}
