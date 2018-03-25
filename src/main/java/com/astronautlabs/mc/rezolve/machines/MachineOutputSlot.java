package com.astronautlabs.mc.rezolve.machines;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class MachineOutputSlot extends Slot {

	public MachineOutputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack.getItem() == null || stack.stackSize == 0;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		ItemStack stack = this.getStack();
		this.inventory.setInventorySlotContents(this.getSlotIndex(), null);
		return stack;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
		
		if (this.inventory instanceof IMachineInventory)
			((IMachineInventory)this.inventory).outputSlotActivated(this.getSlotIndex());
		
		super.onPickupFromSlot(playerIn, stack);
	}

}