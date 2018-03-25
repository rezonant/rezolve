package com.astronautlabs.mc.rezolve.bundles.machines.bundler;

import com.astronautlabs.mc.rezolve.common.*;

import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.util.ItemUtil;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
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

		if (ItemUtil.registered("enderio:itemAlloy")) {
			RecipeUtil.add(
				new ItemStack(this.itemBlock), 
				"VSV",
				"CMC",
				"VFV", 
				
				'V', "item|enderio:itemAlloy|2",
				'S', "block|minecraft:sticky_piston",
				'C', "block|minecraft:chest",
				'M', "item|enderio:itemMachinePart|0",
				'F', "item|enderio:itemBasicFilterUpgrade"
			);
			
		} else {
			RecipeUtil.add(
				new ItemStack(this.itemBlock), 
				"IMI",
				"CSC",
				"IHI", 
				
				'I', "item|minecraft:iron_block",
				'M', "item|minecraft:minecart",
				'C', "item|minecraft:chest",
				'S', "item|minecraft:sticky_piston",
				'H', "item|minecraft:hopper"
			);
		}
	}
}
