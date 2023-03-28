package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.MachineOutputSlot;

import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class BundlePatternOutputSlot extends MachineOutputSlot {

	public BundlePatternOutputSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {

		if (stack.getCount() != 1)
			return false;

		// Don't allow it if there is an item in the output slot (waiting to be taken)

		ItemStack existingStack = this.container.getItem(this.getSlotIndex());
		if (existingStack != null)
			return false;

		// Don't allow it if the item is a blank pattern

		if (RezolveRegistry.item(BundlePatternItem.class).isBlank(stack))
			return false;

		// Otherwise use the basic pattern rules

		if (stack.getItem() != RezolveRegistry.item(BundlePatternItem.class))
			return false;

		return true;
	}

}
