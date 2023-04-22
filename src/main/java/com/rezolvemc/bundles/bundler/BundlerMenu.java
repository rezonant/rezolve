package com.rezolvemc.bundles.bundler;

import com.rezolvemc.bundles.bundleBuilder.BundlePatternSlot;
import com.rezolvemc.common.machines.InputSlot;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.MachineOutputSlot;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.network.WithPacket;
import com.rezolvemc.common.registry.WithScreen;
import net.minecraft.world.entity.player.Inventory;

@WithScreen(BundlerScreen.class)
@WithPacket(BundlerDummyPacket.class)
public class BundlerMenu extends MachineMenu<BundlerEntity> {
	public BundlerMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public BundlerMenu(int containerId, Inventory playerInv, BundlerEntity te) {
		super(RezolveRegistry.menuType(BundlerMenu.class), containerId, playerInv, te);
		int invSlotSize = 18;

		// Input item slots (0-8)

		int inputItemsOffsetX = 20;
		int inputItemsOffsetY = 45;
		int inputItemsWidth = 3;
		int inputItemsHeight = 3;
		int firstInputItemSlot = 0;
		
	    for (int y = 0; y < inputItemsHeight; ++y) {
	        for (int x = 0; x < inputItemsWidth; ++x) {
	            this.addSlot(new InputSlot(container, firstInputItemSlot + x + y * inputItemsWidth, inputItemsOffsetX + x * invSlotSize, inputItemsOffsetY + y * invSlotSize));
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
	            this.addSlot(new BundlePatternSlot(container, firstPatternSlot + x + y * patternsWidth, patternsOffsetX + x * invSlotSize, patternsOffsetY + y * invSlotSize));
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
	            this.addSlot(new MachineOutputSlot(container, firstInvSlot + x + y * invWidth, invOffsetX + x * invSlotSize, invOffsetY + y * invSlotSize));
	        }
	    }

		this.addPlayerSlots(47, 131);
	}
	
}
