package com.rezolvemc.parts;

import com.rezolvemc.common.blocks.BlockBase;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.material.MapColor;

@RegistryId("machine_frame")
public class MachineFrameBlock extends BlockBase {
	public MachineFrameBlock() {
		super(Properties.of().mapColor(MapColor.METAL));
	}

//	@Override
//	public void registerRecipes() {
//
//		RecipeUtil.add(
//			new ItemStack(this.itemBlock),
//			"iCi",
//			"TIT",
//			"iAi",
//
//			'C', "item|minecraft:chest",
//			'T', "item|minecraft:redstone_torch",
//			'I', "item|minecraft:iron_block",
//			'i', "item|rezolve:item_machine_part|1",
//			'A', "item|rezolve:item_machine_part|0"
//		);
//	}
}
