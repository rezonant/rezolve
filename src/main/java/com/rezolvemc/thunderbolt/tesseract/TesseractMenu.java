package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.Sync;
import net.minecraft.world.entity.player.Inventory;

public class TesseractMenu extends MachineMenu<TesseractEntity> {
    public TesseractMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public TesseractMenu(int pContainerId, Inventory playerInventory, TesseractEntity machine) {
        super(pContainerId, playerInventory, machine);
    }
}
