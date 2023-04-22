package com.rezolvemc.storage.machines.storageMonitor;

import org.torchmc.WithScreen;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;

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
