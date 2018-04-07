package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MetalBlock extends MetalStateBlock {
	public MetalBlock() {
		super("block", Material.IRON, 3, 15);
	}

	@Override
	public void registerRecipes() {
		super.registerRecipes();

		for (int i = 0, max = Metal.all().size(); i < max; ++i) {
			// Block -> Ingot
			GameRegistry.addShapelessRecipe(
				RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.get(i), 9),
				RezolveMod.METAL_BLOCK.getStackOf(Metal.get(i))
			);
		}
	}
}
