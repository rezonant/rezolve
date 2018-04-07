package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.item.ItemStack;

public class DiskManipulatorBlock extends Machine {

	public DiskManipulatorBlock() {
		super("block_disk_manipulator");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return DiskManipulatorEntity.class;
	}

	@Override
	public void registerRecipes() {
		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_MANIPULATOR_BLOCK, 1),

			"cDc",
			"RMR",
			"RTR",

			'c', "block_ethernet_cable",
			'D', "item_machine_part|display_panel",
			'R', "mc:redstone",
			'M', "block_machine_frame",
			'T', "item_machine_part|transcoder"
		);
	}
}
