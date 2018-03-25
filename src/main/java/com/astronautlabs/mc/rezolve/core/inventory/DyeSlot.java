package com.astronautlabs.mc.rezolve.core.inventory;

import com.astronautlabs.mc.rezolve.util.ItemUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class DyeSlot extends GhostSlot {

	public DyeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition, true);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack == null || stack.stackSize == 0 || stack.getItem() == null || ItemUtil.isDye(stack.getItem());
	}

}
