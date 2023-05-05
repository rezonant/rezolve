package com.rezolvemc.storage.machines.storageMonitor;

import com.rezolvemc.common.machines.MachineMenu;
import net.minecraft.world.entity.player.Inventory;

public class StorageMonitorMenu extends MachineMenu<StorageMonitorEntity> {
    public StorageMonitorMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public StorageMonitorMenu(int pContainerId, Inventory playerInventory, StorageMonitorEntity machine) {
        super(pContainerId, playerInventory, machine);

        addSlotGrid(0, 3, 3);
        //addSlot(new ResultSlot(playerInventory.player, machine.getCraftMatrix(), machine.getCraftResult(), 0, 64, 149));
        addPlayerSlots();
    }

}
