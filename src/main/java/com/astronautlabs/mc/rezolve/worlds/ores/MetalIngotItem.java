package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class MetalIngotItem extends MetalStateItem {
	public MetalIngotItem() {
		super("ingot");
	}


	@Override
	public void registerRecipes() {
		super.registerRecipes();

		for (int i = 0, max = Metal.all().size(); i < max; ++i) {

			// Ingot -> Nugget
			GameRegistry.addShapelessRecipe(
				RezolveMod.METAL_NUGGET_ITEM.getStackOf(Metal.get(i), 9),
				RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.get(i))
			);

			// 9 Ingot -> Block
			RecipeUtil.add(
				RezolveMod.METAL_BLOCK.getStackOf(Metal.get(i)),

				"iii",
				"iii",
				"iii",

				'i', RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.get(i))
			);
		}


	}
}
