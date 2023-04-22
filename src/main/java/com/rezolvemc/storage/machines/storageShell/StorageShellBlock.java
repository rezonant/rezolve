package com.rezolvemc.storage.machines.storageShell;

import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.material.Material;

@RegistryId("storage_shell")
@WithBlockEntity(StorageShellEntity.class)
@WithMenu(StorageShellMenu.class)
public class StorageShellBlock extends Machine {

	public StorageShellBlock() {
		super(Properties.of(Material.METAL));
	}

//	@Override
//	public void registerRecipes() {
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.STORAGE_SHELL_BLOCK, 1, 0),
//
//			"cDc",
//			"iMi",
//			"pTp",
//
//			'c', "block_ethernet_cable",
//			'D', "item_machine_part|display_panel",
//			'i', "item_machine_part|integrated_circuit",
//			'M', "block_machine_frame",
//			'p', "item_storage_part|0",
//			'T', "item_machine_part|transcoder"
//		);
//	}
}
