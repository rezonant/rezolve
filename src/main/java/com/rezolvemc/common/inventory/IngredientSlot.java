package com.rezolvemc.common.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IngredientSlot extends BaseSlot {

	public IngredientSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		this(inventoryIn, index, xPosition, yPosition, true);
	}
	
	public IngredientSlot(Container inventoryIn, int index, int xPosition, int yPosition, boolean singleItemOnly) {
		super(inventoryIn, index, xPosition, yPosition);
		this.singleItemOnly = singleItemOnly;
	}

	@Override
	public Component getLabel() {
		return Component.translatable("screens.rezolve.ingredient_slot");
	}

	private boolean singleItemOnly;

	public boolean isValidItem(ItemStack stack) {
		return true;
	}

	public boolean isSingleItemOnly() {
		return this.singleItemOnly;
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return false;
	}

	@Override
	public boolean mayPickup(Player pPlayer) {
		return false;
	}
}
