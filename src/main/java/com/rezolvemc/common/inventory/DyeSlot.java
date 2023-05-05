package com.rezolvemc.common.inventory;

import com.rezolvemc.Rezolve;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class DyeSlot extends IngredientSlot {
	public DyeSlot(Container inventoryIn, int index) {
		super(inventoryIn, index, true);
	}

	@Override
	public Component getLabel() {
		return Component.translatable("screens.rezolve.dye_slot");
	}

	@Override
	public boolean isValidItem(ItemStack stack) {
		return stack.isEmpty() || Rezolve.instance().isDye(stack.getItem());
	}
}
