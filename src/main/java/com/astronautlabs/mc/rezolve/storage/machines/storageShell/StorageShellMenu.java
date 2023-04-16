package com.astronautlabs.mc.rezolve.storage.machines.storageShell;

import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;

@WithScreen(StorageShellScreen.class)
public class StorageShellMenu extends MachineMenu<StorageShellEntity> {
    public StorageShellMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public StorageShellMenu(int pContainerId, Inventory playerInventory, StorageShellEntity machine) {
        super(RezolveRegistry.menuType(StorageShellMenu.class), pContainerId, playerInventory, machine);

        addSlotGrid(0, 7, 131, 3, 3);
        addPlayerSlots(88, 131);
    }
}
