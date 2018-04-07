package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.item.ItemStack;

public class DiskBayBlock extends Machine {

	public DiskBayBlock() {
		super("block_disk_bay");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return DiskBayEntity.class;
	}

	@Override
	public void registerRecipes() {
		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_BAY_BLOCK, 1),

			"cAc",
			"RMR",
			"RPR",

			'c', "block_ethernet_cable",
			'D', "item_machine_part|activator",
			'R', "mc:redstone",
			'M', "block_machine_frame",
			'P', "item_storage_part|0"
		);
	}
}
