package com.astronautlabs.mc.rezolve.machines.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.inventory.DyeSlot;
import com.astronautlabs.mc.rezolve.inventory.GhostSlot;
import com.astronautlabs.mc.rezolve.machines.MachineOutputSlot;

import net.minecraft.inventory.IInventory;

public class BundleBuilderContainer extends ContainerBase<BundleBuilderEntity> {
	
	public BundleBuilderContainer(IInventory playerInv, BundleBuilderEntity te) {
		super(te);
		int invSlotSize = 18;

		// Pattern/Dye Slots

        this.addSlotToContainer(new MachineOutputSlot(te, BundleBuilderEntity.PATTERN_OUTPUT_SLOT, 137, 59));		// Pattern slot
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
		int playerHotbarOffsetX = 29;
		int playerHotbarOffsetY = 189;

		this.addPlayerSlots(playerInv, playerInvOffsetX, playerInvOffsetY, invSlotSize, playerHotbarOffsetX, playerHotbarOffsetY);
	}
}
