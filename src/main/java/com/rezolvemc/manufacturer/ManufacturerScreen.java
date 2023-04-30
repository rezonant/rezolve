package com.rezolvemc.manufacturer;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

@ScreenFor(ManufacturerMenu.class)
public class ManufacturerScreen extends MachineScreen<ManufacturerMenu> {
    public ManufacturerScreen(ManufacturerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 256, 256);
    }

    @Override
    protected void setup() {
        super.setup();


    }
}
