package com.rezolvemc.bundles.bundler;

import com.rezolvemc.Rezolve;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import org.torchmc.inventory.InputSlot;

public class BundlerInputSlot extends InputSlot {
    public BundlerInputSlot(Container pContainer, int pSlot) {
        super(pContainer, pSlot);
    }

    @Override
    public Component getHint() {
        return Rezolve.str("bundler_input_slot_hint");
    }
}
