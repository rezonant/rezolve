package org.torchmc.inventory;

import org.torchmc.inventory.BaseSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

public class InputSlot extends BaseSlot {
    public InputSlot(Container pContainer, int pSlot) {
        super(pContainer, pSlot);
    }

    @Override
    public Component getLabel() {
        return Component.translatable("screens.rezolve.input_slot");
    }
}
