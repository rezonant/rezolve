package com.astronautlabs.mc.rezolve.securityServer;

import com.astronautlabs.mc.rezolve.RezolveMod;

import com.astronautlabs.mc.rezolve.common.MachineMenu;
import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.RegistryObject;

public class SecurityServerMenu extends MachineMenu<SecurityServerEntity> {
	public SecurityServerMenu(int containerId, Inventory playerInventory, SecurityServerEntity entity) {
		super(RezolveRegistry.menuType(SecurityServerMenu.class), containerId, playerInventory, entity);
	}

	public SecurityServerMenu(int containerId, Inventory playerInventory) {
		super(RezolveRegistry.menuType(SecurityServerMenu.class), containerId, playerInventory, null);
	}

}
