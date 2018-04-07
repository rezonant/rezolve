package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MetalOreBlock extends MetalStateBlock {
	public MetalOreBlock() {
		super("ore", Material.ROCK, 3, 15);
	}

	@Override
	public void registerRecipes() {
		super.registerRecipes();

		// Ore -> Ingot
		for (int i = 0, max = Metal.all().size(); i < max; ++i)
			GameRegistry.addSmelting(new ItemStack(this, 1, i), RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.get(i)), 1);
	}
}
