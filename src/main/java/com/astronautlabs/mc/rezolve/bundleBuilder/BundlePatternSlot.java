package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BundlePatternSlot extends Slot {
	public BundlePatternSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return pStack.getItem() == RezolveRegistry.item(BundlePatternItem.class);
	}
}
