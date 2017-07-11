package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.common.DyeSlot;
import com.astronautlabs.mc.rezolve.common.GhostSlot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BundleBuilderContainer extends ContainerBase<BundleBuilderEntity> {
	
	public BundleBuilderContainer(IInventory playerInv, BundleBuilderEntity te) {
		super(te);
		int invSlotSize = 18;

		// Pattern/Dye Slots

        this.addSlotToContainer(new BundlePatternOutputSlot(te, BundleBuilderEntity.PATTERN_OUTPUT_SLOT, 137, 59));		// Pattern slot
        this.addSlotToContainer(new BundlePatternSlot(te, BundleBuilderEntity.PATTERN_INPUT_SLOT, 101, 59));		// Pattern slot
        this.addSlotToContainer(new DyeSlot(te, BundleBuilderEntity.DYE_SLOT, 119, 77));					// Dye slot
        
	    // Bundle Items Slots
		
		int invOffsetX = 11;
		int invOffsetY = 41;
		int invWidth = 3;
		int invHeight = 3;
		
	    for (int y = 0; y < invHeight; ++y) {
	        for (int x = 0; x < invWidth; ++x) {
	            this.addSlotToContainer(new GhostSlot(te, 3 + x + y * invWidth, invOffsetX + x * invSlotSize, invOffsetY + y * invSlotSize, false));
	        }
	    }

		int playerInvOffsetX = 29;
		int playerInvOffsetY = 131;
		
	    // Player Inventory, Slot 9-35, Slot IDs 9-35
	    for (int y = 0; y < 3; ++y) {
	        for (int x = 0; x < 9; ++x) {
	            this.addSlotToContainer(new Slot(playerInv, 9 + x + y * 9, playerInvOffsetX + x * invSlotSize, playerInvOffsetY + y * invSlotSize));
	        }
	    }

	    int playerHotbarOffsetX = 29;
	    int playerHotbarOffsetY = 189;
	    
	    // Player Inventory, Slot 0-8, Slot IDs 36-44
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
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		// TODO Auto-generated method stub
		return this.entity.isUseableByPlayer(player);
	}
	
}
