package com.rezolvemc.storage.machines.diskBay;

import com.rezolvemc.common.registry.WithScreen;
import com.rezolvemc.common.inventory.ValidatedSlot;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;

@WithScreen(DiskBayScreen.class)
public class DiskBayMenu extends MachineMenu<DiskBayEntity> {
    public DiskBayMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public DiskBayMenu(int containerId, Inventory playerInv, DiskBayEntity te) {
        super(RezolveRegistry.menuType(DiskBayMenu.class), containerId, playerInv, te);

        addSlotGrid(
                0,
                (id, x, y) -> new ValidatedSlot(container, id, x, y, stack -> DiskBayEntity.isValidDisk(stack)),
                47, 45, 9, 3
        );

        addPlayerSlots(47, 131);
    }
}
