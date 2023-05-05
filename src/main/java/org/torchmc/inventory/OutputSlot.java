package org.torchmc.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.torchmc.inventory.BaseSlot;

public class OutputSlot extends BaseSlot {
    public OutputSlot(Container pContainer, int pSlot) {
        super(pContainer, pSlot);
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
