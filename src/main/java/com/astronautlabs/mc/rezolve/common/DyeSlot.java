package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class DyeSlot extends GhostSlot {

	public DyeSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition, true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return RezolveMod.instance().isDye(pStack.getItem());
	}
}
