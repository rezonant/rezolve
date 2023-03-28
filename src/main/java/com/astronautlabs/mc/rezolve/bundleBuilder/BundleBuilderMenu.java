package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.bundler.BundlerMenu;
import com.astronautlabs.mc.rezolve.common.*;
import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.registries.RegistryObject;

public class BundleBuilderMenu extends MachineMenu<BundleBuilderEntity> {
	public BundleBuilderMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, null);
	}

	public BundleBuilderMenu(int containerId, Inventory playerInventory, BundleBuilderEntity te) {
		super(RezolveRegistry.menuType(BundleBuilderMenu.class), containerId, playerInventory, te);
		int invSlotSize = 18;

		// Pattern/Dye Slots

        this.addSlot(new MachineOutputSlot(te, BundleBuilderEntity.PATTERN_OUTPUT_SLOT, 137, 59));		// Pattern slot
        this.addSlot(new BundlePatternSlot(te, BundleBuilderEntity.PATTERN_INPUT_SLOT, 101, 59));		// Pattern slot
        this.addSlot(new DyeSlot(te, BundleBuilderEntity.DYE_SLOT, 119, 77));					// Dye slot
        
	    // Bundle Items Slots
		
		int invOffsetX = 11;
		int invOffsetY = 41;
		int invWidth = 3;
		int invHeight = 3;
		
	    for (int y = 0; y < invHeight; ++y) {
	        for (int x = 0; x < invWidth; ++x) {
	            this.addSlot(new GhostSlot(te, 3 + x + y * invWidth, invOffsetX + x * invSlotSize, invOffsetY + y * invSlotSize, false));
	        }
	    }

		int playerInvOffsetX = 29;
		int playerInvOffsetY = 131;
		
	    // Player Inventory, Slot 9-35, Slot IDs 9-35
	    for (int y = 0; y < 3; ++y) {
	        for (int x = 0; x < 9; ++x) {
	            this.addSlot(new Slot(playerInventory, 9 + x + y * 9, playerInvOffsetX + x * invSlotSize, playerInvOffsetY + y * invSlotSize));
	        }
	    }

	    int playerHotbarOffsetX = 29;
	    int playerHotbarOffsetY = 189;
	    
	    // Player Inventory, Slot 0-8, Slot IDs 36-44
	    for (int x = 0; x < 9; ++x) {
	        this.addSlot(new Slot(playerInventory, x, playerHotbarOffsetX + x * 18, playerHotbarOffsetY));
	    }
	}
}
