package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.ItemBase;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlankBundlePatternItem extends ItemBase {
	public BlankBundlePatternItem() {
		super("item_bundle_pattern_blank");
	}

	@Override
	public void registerRecipes() {

		GameRegistry.addRecipe(new ItemStack(this), 
			"OSO",
			"GIG",
			"OSO", 
			
			'O', Item.REGISTRY.getObject(new ResourceLocation("minecraft:obsidian")),
			'S', Item.REGISTRY.getObject(new ResourceLocation("minecraft:slime_ball")),
			'G', Item.REGISTRY.getObject(new ResourceLocation("minecraft:glowstone_dust")),
			'I', Item.REGISTRY.getObject(new ResourceLocation("minecraft:item_frame"))
		);
	}
}
