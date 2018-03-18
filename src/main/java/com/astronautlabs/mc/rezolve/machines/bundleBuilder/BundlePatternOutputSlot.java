package com.astronautlabs.mc.rezolve.machines.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.machines.MachineOutputSlot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class BundlePatternOutputSlot extends MachineOutputSlot {

	public BundlePatternOutputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		
		if (stack.stackSize != 1)
			return false;
		
		// Don't allow it if there is an item in the output slot (waiting to be taken) 
		
		ItemStack existingStack = this.inventory.getStackInSlot(this.getSlotIndex());
		if (existingStack != null)
			return false;
		
		// Don't allow it if the item is a blank pattern
		
		if (RezolveMod.BUNDLE_PATTERN_ITEM.isBlank(stack))
			return false;
		
		// Otherwise use the basic pattern rules

		if (stack.getItem() != RezolveMod.BUNDLE_PATTERN_ITEM)
			return false;

		return true;
	}

}
