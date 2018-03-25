package com.astronautlabs.mc.rezolve.core.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class GhostSlot extends Slot {

	public GhostSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		this(inventoryIn, index, xPosition, yPosition, true);
	}
	
	public GhostSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean singleItemOnly) {
		super(inventoryIn, index, xPosition, yPosition);
		
		this.singleItemOnly = singleItemOnly;
	}
	
	private boolean singleItemOnly;
	
	public boolean isSingleItemOnly() {
		return this.singleItemOnly;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
	
	@Override
	public ItemStack getStack() {
		// TODO Auto-generated method stub
		return super.getStack();
	}

}
