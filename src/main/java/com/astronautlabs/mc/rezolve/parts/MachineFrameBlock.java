package com.astronautlabs.mc.rezolve.parts;

import com.astronautlabs.mc.rezolve.common.BlockBase;
import com.astronautlabs.mc.rezolve.util.ItemUtil;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.item.ItemStack;

public class MachineFrameBlock extends BlockBase {

	public MachineFrameBlock() {
		super("block_machine_frame");
	}
	@Override
	public void registerRecipes() {

		RecipeUtil.add(
			new ItemStack(this.itemBlock),
			"iCi",
			"TIT",
			"iAi",

			'C', "item|minecraft:chest",
			'T', "item|minecraft:redstone_torch",
			'I', "item|minecraft:iron_block",
			'i', "item|rezolve:item_machine_part|1",
			'A', "item|rezolve:item_machine_part|0"
		);
	}
}
