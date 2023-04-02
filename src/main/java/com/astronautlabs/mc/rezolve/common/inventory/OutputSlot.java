package com.astronautlabs.mc.rezolve.common.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class OutputSlot extends BaseSlot {
    public OutputSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return false;
    }

    @Override
    public Component getLabel() {
        return Component.translatable("screens.rezolve.output_slot");
    }
}
