package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.inventory.BaseSlot;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class BundlePatternSlot extends BaseSlot {
	public BundlePatternSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return pStack == null || pStack.isEmpty() || pStack.getItem() == RezolveRegistry.item(BundlePatternItem.class);
	}

	@Override
	public Component getLabel() {
		return Component.translatable("screens.rezolve.bundle_pattern_input_slot");
	}
}
