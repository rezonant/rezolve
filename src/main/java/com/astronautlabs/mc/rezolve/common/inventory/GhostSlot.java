package com.astronautlabs.mc.rezolve.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GhostSlot extends Slot {

	public GhostSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		this(inventoryIn, index, xPosition, yPosition, true);
	}
	
	public GhostSlot(Container inventoryIn, int index, int xPosition, int yPosition, boolean singleItemOnly) {
		super(inventoryIn, index, xPosition, yPosition);
		
		this.singleItemOnly = singleItemOnly;
	}
	
	private boolean singleItemOnly;
	
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
