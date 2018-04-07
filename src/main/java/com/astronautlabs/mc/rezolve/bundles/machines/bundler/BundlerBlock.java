package com.astronautlabs.mc.rezolve.bundles.machines.bundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.*;

import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.util.ItemUtil;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import com.astronautlabs.mc.rezolve.worlds.ores.Metal;
import net.minecraft.item.ItemStack;

public class BundlerBlock extends Machine {
	public BundlerBlock() {
		super("block_bundler");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return BundlerEntity.class;
	}
	
	@Override
	public void registerRecipes() {
		RecipeUtil.add(
			new ItemStack(this.itemBlock),
			"cSc",
			"CMC",
			"cFc",

			'c', RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.COPPER),
			'S', "mc:sticky_piston",
			'C', "mc:chest",
			'M', "block_machine_frame",
			'F', "item_machine_part|integrated_circuit"
		);
	}
}
