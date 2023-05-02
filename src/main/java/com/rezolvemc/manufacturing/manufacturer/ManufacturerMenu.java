package com.rezolvemc.manufacturing.manufacturer;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;

public class ManufacturerMenu extends MachineMenu<ManufacturerEntity> {
    public ManufacturerMenu(int pContainerId, Inventory playerInventory, ManufacturerEntity machine) {
        super(RezolveRegistry.menuType(ManufacturerMenu.class), pContainerId, playerInventory, machine);
    }

    public ManufacturerMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }
}
