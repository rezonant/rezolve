package com.rezolvemc.bundles.bundler;

import com.rezolvemc.bundles.bundleBuilder.BundlePatternSlot;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.MachineOutputSlot;
import net.minecraft.world.entity.player.Inventory;

public class BundlerMenu extends MachineMenu<BundlerEntity> {
	public BundlerMenu(int containerId, Inventory playerInv) {
		this(containerId, playerInv, null);
	}

	public BundlerMenu(int containerId, Inventory playerInv, BundlerEntity te) {
		super(containerId, playerInv, te);

		addSlotGrid(0, id -> new BundlerInputSlot(container, id), 3, 3); // Input
		addSlotGrid(9, id -> new BundlePatternSlot(container, id), 3, 3); // Pattern
		addSlotGrid(18, id -> new MachineOutputSlot(container, id), 3, 3); // Output

		this.addPlayerSlots();
	}
	
}
