package com.astronautlabs.mc.rezolve.thunderbolt.securityServer;

import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;

public class SecurityServerMenu extends MachineMenu<SecurityServerEntity> {
	public SecurityServerMenu(int containerId, Inventory playerInventory, SecurityServerEntity entity) {
		super(RezolveRegistry.menuType(SecurityServerMenu.class), containerId, playerInventory, entity);
	}

	public SecurityServerMenu(int containerId, Inventory playerInventory) {
		super(RezolveRegistry.menuType(SecurityServerMenu.class), containerId, playerInventory, null);
	}

}
