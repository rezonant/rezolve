package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.bundles.BundleItem;

import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BundleSlot extends Slot {
	public BundleSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() == RezolveRegistry.item(BundleItem.class);
	}
}
