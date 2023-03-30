package com.astronautlabs.mc.rezolve.bundles.unbundler;

import com.astronautlabs.mc.rezolve.bundles.bundler.BundleSlot;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.machines.MachineOutputSlot;

import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class UnbundlerMenu extends MachineMenu<UnbundlerEntity> {
	public UnbundlerMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public UnbundlerMenu(int containerId, Inventory playerInv, UnbundlerEntity te) {
		super(RezolveRegistry.menuType(UnbundlerMenu.class), containerId, playerInv, te);

		int invSlotSize = 18;

		// Input bundles slots (0-8)
		int inputItemsOffsetX = 47;
		int inputItemsOffsetY = 55;
		int inputItemsWidth = 3;
		int inputItemsHeight = 3;
		int firstInputItemSlot = 0;
		
	    for (int y = 0; y < inputItemsHeight; ++y) {
	        for (int x = 0; x < inputItemsWidth; ++x) {
	            this.addSlot(new BundleSlot(container, firstInputItemSlot + x + y * inputItemsWidth, inputItemsOffsetX + x * invSlotSize, inputItemsOffsetY + y * invSlotSize));
	        }
	    }
	    
	    // Output items slots 10-25
		
		int patternsOffsetX = 137;
		int patternsOffsetY = 37;
		int patternsWidth = 4;
		int patternsHeight = 4;
		int firstPatternSlot = 9;
		
	    for (int y = 0; y < patternsHeight; ++y) {
	        for (int x = 0; x < patternsWidth; ++x) {
	            this.addSlot(new MachineOutputSlot(container, firstPatternSlot + x + y * patternsWidth, patternsOffsetX + x * invSlotSize, patternsOffsetY + y * invSlotSize));
	        }
	    }
	    
		int playerInvOffsetX = 47;
		int playerInvOffsetY = 131;
		
	    // Player Inventory, slots 9-35
		
	    for (int y = 0; y < 3; ++y) {
	        for (int x = 0; x < 9; ++x) {
	            this.addSlot(new Slot(playerInv, 9 + x + y * 9, playerInvOffsetX + x * invSlotSize, playerInvOffsetY + y * invSlotSize));
	        }
	    }

	    int playerHotbarOffsetX = 47;
	    int playerHotbarOffsetY = 189;
	    
	    // Player Hotbar, slots 0-8
	    
	    for (int x = 0; x < 9; ++x) {
	        this.addSlot(new Slot(playerInv, x, playerHotbarOffsetX + x * 18, playerHotbarOffsetY));
	    }
	}
}
