package com.astronautlabs.mc.rezolve.unbundler;

import com.astronautlabs.mc.rezolve.bundleBuilder.BundlePatternSlot;
import com.astronautlabs.mc.rezolve.common.MachineOutputSlot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class UnbundlerContainer extends Container {
	
	public UnbundlerContainer(IInventory playerInv, UnbundlerEntity te) {
		this.entity = te;
		int invSlotSize = 18;

		// Input item slots (0-9)
		int inputItemsOffsetX = 11;
		int inputItemsOffsetY = 37;
		int inputItemsWidth = 10;
		int inputItemsHeight = 1;
		int firstInputItemSlot = 0;
		
	    for (int y = 0; y < inputItemsHeight; ++y) {
	        for (int x = 0; x < inputItemsWidth; ++x) {
	            this.addSlotToContainer(new Slot(te, firstInputItemSlot + x + y * inputItemsWidth, inputItemsOffsetX + x * invSlotSize, inputItemsOffsetY + y * invSlotSize));
	        }
	    }
	    
	    // Pattern slots 10-19
		
		int patternsOffsetX = 11;
		int patternsOffsetY = 67;
		int patternsWidth = 10;
		int patternsHeight = 1;
		int firstPatternSlot = 10;
		
	    for (int y = 0; y < patternsHeight; ++y) {
	        for (int x = 0; x < patternsWidth; ++x) {
	            this.addSlotToContainer(new BundlePatternSlot(te, firstPatternSlot + x + y * patternsWidth, patternsOffsetX + x * invSlotSize, patternsOffsetY + y * invSlotSize));
	        }
	    }
	    
	    // Output slots 20-29
		
		int invOffsetX = 11;
		int invOffsetY = 97;
		int invWidth = 10;
		int invHeight = 1;
		int firstInvSlot = 20;
		
	    for (int y = 0; y < invHeight; ++y) {
	        for (int x = 0; x < invWidth; ++x) {
	            this.addSlotToContainer(new MachineOutputSlot(te, firstInvSlot + x + y * invWidth, invOffsetX + x * invSlotSize, invOffsetY + y * invSlotSize));
	        }
	    }

		int playerInvOffsetX = 47;
		int playerInvOffsetY = 131;
		
	    // Player Inventory, slots 9-35
		
	    for (int y = 0; y < 3; ++y) {
	        for (int x = 0; x < 9; ++x) {
	            this.addSlotToContainer(new Slot(playerInv, 9 + x + y * 9, playerInvOffsetX + x * invSlotSize, playerInvOffsetY + y * invSlotSize));
	        }
	    }

	    int playerHotbarOffsetX = 47;
	    int playerHotbarOffsetY = 189;
	    
	    // Player Hotbar, slots 0-8
	    
	    for (int x = 0; x < 9; ++x) {
	        this.addSlotToContainer(new Slot(playerInv, x, playerHotbarOffsetX + x * 18, playerHotbarOffsetY));
	    }
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
	    ItemStack previous = null;
	    Slot slot = (Slot) this.inventorySlots.get(fromSlot);

	    if (slot != null && slot.getHasStack()) {
	        ItemStack current = slot.getStack();
	        previous = current.copy();

	        if (fromSlot < 9) {
	            // From TE Inventory to Player Inventory
	            if (!this.mergeItemStack(current, 9, 45, true))
	                return null;
	        } else {
	            // From Player Inventory to TE Inventory
	            if (!this.mergeItemStack(current, 0, 9, false))
	                return null;
	        }

	        if (current.stackSize == 0)
	            slot.putStack((ItemStack) null);
	        else
	            slot.onSlotChanged();

	        if (current.stackSize == previous.stackSize)
	            return null;
	        slot.onPickupFromSlot(playerIn, current);
	    }
	    return previous;
	}
	
	private UnbundlerEntity entity;
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		// TODO Auto-generated method stub
		return this.entity.isUseableByPlayer(player);
	}
	
}
