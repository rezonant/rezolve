package com.rezolvemc.storage.machines.diskBay;

import com.rezolvemc.common.blocks.WithBlockEntity;
import org.torchmc.WithMenu;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("disk_bay")
@WithBlockEntity(DiskBayEntity.class)
@WithMenu(DiskBayMenu.class)
public class DiskBayBlock extends Machine {
	public DiskBayBlock() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}

//	@Override
//	public void registerRecipes() {
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_BAY_BLOCK, 1),
//
//			"cAc",
//			"RMR",
//			"RPR",
//
//			'c', "block_ethernet_cable",
//			'D', "item_machine_part|activator",
//			'R', "mc:redstone",
//			'M', "block_machine_frame",
//			'P', "item_storage_part|0"
//		);
//	}
}
