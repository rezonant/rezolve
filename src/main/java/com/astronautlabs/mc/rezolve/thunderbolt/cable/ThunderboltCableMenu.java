package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.machines.Sync;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;

@WithScreen(ThunderboltCableScreen.class)
public class ThunderboltCableMenu extends MachineMenu<ThunderboltCableEntity> {
    public ThunderboltCableMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public ThunderboltCableMenu(int pContainerId, Inventory playerInventory, ThunderboltCableEntity machine) {
        super(RezolveRegistry.menuType(ThunderboltCableMenu.class), pContainerId, playerInventory, machine);
    }

    @Sync public Direction direction;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (machine == null) {

        } else {
            this.direction = direction;
        }
    }
}
