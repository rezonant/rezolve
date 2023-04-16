package com.astronautlabs.mc.rezolve.storage.machines.storageMonitor;

import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultSlot;

@WithScreen(StorageMonitorScreen.class)
public class StorageMonitorMenu extends MachineMenu<StorageMonitorEntity> {
    public StorageMonitorMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public StorageMonitorMenu(int pContainerId, Inventory playerInventory, StorageMonitorEntity machine) {
        super(RezolveRegistry.menuType(StorageMonitorMenu.class), pContainerId, playerInventory, machine);

        addSlotGrid(0, 7, 131, 3, 3);
        //addSlot(new ResultSlot(playerInventory.player, machine.getCraftMatrix(), machine.getCraftResult(), 0, 64, 149));
        addPlayerSlots(88, 131);
    }

}
