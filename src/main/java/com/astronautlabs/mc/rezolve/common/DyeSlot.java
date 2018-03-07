package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class DyeSlot extends GhostSlot {

	public DyeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition, true);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack == null || stack.stackSize == 0 || stack.getItem() == null || RezolveMod.instance().isDye(stack.getItem());
	}

}
