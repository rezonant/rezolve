package com.rezolvemc.common.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.torchmc.inventory.BaseSlot;

public class IngredientSlot extends BaseSlot {

	public IngredientSlot(Container inventoryIn, int index) {
		this(inventoryIn, index, true);
	}
	
	public IngredientSlot(Container inventoryIn, int index, boolean singleItemOnly) {
		super(inventoryIn, index);
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
