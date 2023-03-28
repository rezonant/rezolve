package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BundleSlot extends Slot {
	public BundleSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack == null) {
			System.out.println("THATS IT");
		}

		if (stack.getItem() instanceof BundleItem)
			return true;

		return false;
	}
}
