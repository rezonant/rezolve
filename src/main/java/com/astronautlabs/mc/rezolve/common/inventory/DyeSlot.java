package com.astronautlabs.mc.rezolve.common.inventory;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class DyeSlot extends IngredientSlot {
	public DyeSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition, true);
	}

	@Override
	public Component getLabel() {
		return Component.translatable("screens.rezolve.dye_slot");
	}

	@Override
	public boolean isValidItem(ItemStack stack) {
		return RezolveMod.instance().isDye(stack.getItem());
	}
}
