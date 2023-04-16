package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.bundles.bundler.BundlerEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.inventory.ValidatedSlot;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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
