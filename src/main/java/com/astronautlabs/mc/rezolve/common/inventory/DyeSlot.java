package com.astronautlabs.mc.rezolve.common.inventory;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class DyeSlot extends GhostSlot {
	public DyeSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition, true);
	}

	@Override
	public boolean isValidItem(ItemStack stack) {
		return RezolveMod.instance().isDye(stack.getItem());
	}
}
