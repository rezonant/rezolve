package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class MetalNuggetItem extends MetalStateItem {
	public MetalNuggetItem() {
		super("nugget");
	}

	@Override
	public void registerRecipes() {
		super.registerRecipes();

		// Nugget -> Ingot
		for (int i = 0, max = Metal.all().size(); i < max; ++i) {
			RecipeUtil.add(
				RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.get(i)),

				"nnn",
				"nnn",
				"nnn",

				'n', RezolveMod.METAL_NUGGET_ITEM.getStackOf(Metal.get(i))
			);
		}
	}
}
