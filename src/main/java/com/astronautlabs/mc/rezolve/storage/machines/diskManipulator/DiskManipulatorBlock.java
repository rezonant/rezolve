package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.world.level.material.Material;

@RegistryId("disk_manipulator")
@WithBlockEntity(DiskManipulatorEntity.class)
@WithMenu(DiskManipulatorMenu.class)
public class DiskManipulatorBlock extends Machine {
	public DiskManipulatorBlock() {
		super(Properties.of(Material.METAL));
	}

//	@Override
//	public void registerRecipes() {
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_MANIPULATOR_BLOCK, 1),
//
//			"cDc",
//			"RMR",
//			"RTR",
//
//			'c', "block_ethernet_cable",
//			'D', "item_machine_part|display_panel",
//			'R', "mc:redstone",
//			'M', "block_machine_frame",
//			'T', "item_machine_part|transcoder"
//		);
//	}
}
