package org.torchmc.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class BaseSlot extends Slot {
    public BaseSlot(Container pContainer, int pSlot) {
        super(pContainer, pSlot, 0, 0);
    }

    /**
     * Used to label this slot in the UI (ie in tooltips). Example "Dye Slot".
     *
     * @return
     */
    public Component getLabel() {
        return null;
    }

    public Component getHint() { return null; }
}
