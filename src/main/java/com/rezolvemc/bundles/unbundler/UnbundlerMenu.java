package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.bundles.bundler.BundleSlot;
import com.rezolvemc.common.registry.WithScreen;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.MachineOutputSlot;

import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.world.entity.player.Inventory;

@WithScreen(UnbundlerScreen.class)
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

		addPlayerSlots(47, 131);;
	}
}
