package com.rezolvemc.manufacturing.manufacturer;

import com.rezolvemc.common.machines.MachineMenu;
import net.minecraft.world.entity.player.Inventory;

public class ManufacturerMenu extends MachineMenu<ManufacturerEntity> {
    public ManufacturerMenu(int pContainerId, Inventory playerInventory, ManufacturerEntity machine) {
        super(pContainerId, playerInventory, machine);
    }

    public ManufacturerMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }
}
