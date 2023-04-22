package com.rezolvemc.bundles.bundler;

import com.rezolvemc.bundles.BundleItem;
import com.rezolvemc.common.inventory.BaseSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class BundleSlot extends BaseSlot {
	public BundleSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.getItem() instanceof BundleItem)
			return true;

		return false;
	}

	@Override
	public Component getLabel() {
		return Component.translatable("screens.rezolve.bundle_input_slot");
	}
}
