package com.astronautlabs.mc.rezolve.machines.unbundler;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BundleSlot extends Slot {
	
	public BundleSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem() == RezolveMod.BUNDLE_ITEM;
	}
}
