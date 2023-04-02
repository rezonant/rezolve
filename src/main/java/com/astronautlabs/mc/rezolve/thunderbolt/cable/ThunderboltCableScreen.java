package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ThunderboltCableScreen extends MachineScreen<ThunderboltCableMenu> {
    public ThunderboltCableScreen(ThunderboltCableMenu menu, Inventory playerInventory, Component pTitle) {
        super(menu, playerInventory, pTitle, "rezolve:textures/gui/container/thunderbolt_cable_gui.png", 256, 256);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.rezolve.thunderbolt_connection");
    }
}
