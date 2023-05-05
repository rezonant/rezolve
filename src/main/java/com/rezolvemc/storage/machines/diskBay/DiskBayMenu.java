package com.rezolvemc.storage.machines.diskBay;

import org.torchmc.inventory.ValidatedSlot;
import com.rezolvemc.common.machines.MachineMenu;
import net.minecraft.world.entity.player.Inventory;

public class DiskBayMenu extends MachineMenu<DiskBayEntity> {
    public DiskBayMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public DiskBayMenu(int containerId, Inventory playerInv, DiskBayEntity te) {
        super(containerId, playerInv, te);

        addSlotGrid(
                0,
                id -> new ValidatedSlot(container, id, stack -> DiskBayEntity.isValidDisk(stack)),
                9, 3
        );

        addPlayerSlots();
    }
}
