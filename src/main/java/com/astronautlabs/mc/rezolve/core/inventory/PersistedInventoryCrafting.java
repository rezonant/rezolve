package com.astronautlabs.mc.rezolve.core.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class PersistedInventoryCrafting extends InventoryCrafting {
	public PersistedInventoryCrafting(IInventory inventory, int baseIndex, Container eventHandlerIn, int width, int height) {
		super(eventHandlerIn, width, height);
		this.inventory = inventory;
		this.baseIndex = baseIndex;
	}

	private IInventory inventory;
	private int baseIndex = 0;

	@Override
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
		super.setInventorySlotContents(index, stack);
		this.inventory.setInventorySlotContents(this.baseIndex + index, stack);
	}

	@Nullable
	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack result = super.decrStackSize(index, count);

		this.inventory.setInventorySlotContents(this.baseIndex + index, this.getStackInSlot(index));

		return result;
	}
}
