package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BundlePatternSlot extends Slot {

	public BundlePatternSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {

		if (!RezolveMod.instance().isBundlePatternItem(stack.getItem()))
			return false;

		return super.isItemValid(stack);
	}

}
