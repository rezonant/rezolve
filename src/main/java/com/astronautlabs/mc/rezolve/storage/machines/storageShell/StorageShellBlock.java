package com.astronautlabs.mc.rezolve.storage.machines.storageShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.item.ItemStack;

public class StorageShellBlock extends Machine {

	public StorageShellBlock() {
		super("block_storage_shell");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return StorageShellEntity.class;
	}

	@Override
	public void registerRecipes() {
		RecipeUtil.add(
			new ItemStack(RezolveMod.STORAGE_SHELL_BLOCK, 1, 0),

			"cDc",
			"iMi",
			"pTp",

			'c', "block_ethernet_cable",
			'D', "item_machine_part|display_panel",
			'i', "item_machine_part|integrated_circuit",
			'M', "block_machine_frame",
			'p', "item_storage_part|0",
			'T', "item_machine_part|transcoder"
		);
	}
}
