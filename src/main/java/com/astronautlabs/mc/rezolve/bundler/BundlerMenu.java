package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.bundleBuilder.BundlePatternSlot;
import com.astronautlabs.mc.rezolve.common.MachineMenu;
import com.astronautlabs.mc.rezolve.common.MachineOutputSlot;
import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.registry.WithScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.registries.RegistryObject;

@WithScreen(BundlerScreen.class)
public class BundlerMenu extends MachineMenu<BundlerEntity> {
	public BundlerMenu(int containerId, Inventory playerInv) {
		super(RezolveRegistry.menuType(BundlerMenu.class), containerId, playerInv, null);
	}

	public BundlerMenu(int containerId, Inventory playerInv, BundlerEntity te) {
		super(RezolveRegistry.menuType(BundlerMenu.class), containerId, playerInv, te);
		int invSlotSize = 18;

		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return 0;
			}

			@Override
			public void set(int pValue) {

			}
		});

		// Input item slots (0-8)
		int inputItemsOffsetX = 20;
		int inputItemsOffsetY = 45;
		int inputItemsWidth = 3;
		int inputItemsHeight = 3;
		int firstInputItemSlot = 0;
		
	    for (int y = 0; y < inputItemsHeight; ++y) {
	        for (int x = 0; x < inputItemsWidth; ++x) {
	            this.addSlot(new Slot(te, firstInputItemSlot + x + y * inputItemsWidth, inputItemsOffsetX + x * invSlotSize, inputItemsOffsetY + y * invSlotSize));
	        }
	    }
	    
	    // Pattern slots 9-17
		
		int patternsOffsetX = 81;
		int patternsOffsetY = 45;
		int patternsWidth = 3;
		int patternsHeight = 3;
		int firstPatternSlot = 9;
		
	    for (int y = 0; y < patternsHeight; ++y) {
	        for (int x = 0; x < patternsWidth; ++x) {
	            this.addSlot(new BundlePatternSlot(te, firstPatternSlot + x + y * patternsWidth, patternsOffsetX + x * invSlotSize, patternsOffsetY + y * invSlotSize));
	        }
	    }
	    
	    // Output slots 18-26
		
		int invOffsetX = 165;
		int invOffsetY = 45;
		int invWidth = 3;
		int invHeight = 3;
		int firstInvSlot = 18;
		
	    for (int y = 0; y < invHeight; ++y) {
	        for (int x = 0; x < invWidth; ++x) {
	            this.addSlot(new MachineOutputSlot(te, firstInvSlot + x + y * invWidth, invOffsetX + x * invSlotSize, invOffsetY + y * invSlotSize));
	        }
	    }

		this.addPlayerSlots(47, 131);
	}
	
}
