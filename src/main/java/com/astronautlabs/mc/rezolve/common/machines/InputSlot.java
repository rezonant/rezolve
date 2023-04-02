package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.common.inventory.BaseSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

public class InputSlot extends BaseSlot {
    public InputSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public Component getLabel() {
        return Component.translatable("screens.rezolve.input_slot");
    }
}
