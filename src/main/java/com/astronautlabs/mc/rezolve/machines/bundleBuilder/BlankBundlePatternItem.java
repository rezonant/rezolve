package com.astronautlabs.mc.rezolve.machines.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ItemBase;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BlankBundlePatternItem extends ItemBase {
	public BlankBundlePatternItem() {
		super("item_bundle_pattern_blank");
	}

	@Override
	public void registerRecipes() {

		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {

			RecipeUtil.add(new ItemStack(this),
				"PSP",
				"GFG",
				"PSP", 
				
				'P', "item|enderio:itemAlloy|5",
				'S', "item|enderio:itemMaterial",
				'G', Items.GLOWSTONE_DUST,
				'F', "item|enderio:itemBasicFilterUpgrade"
			);
			
		} else {
			RecipeUtil.add(new ItemStack(this),
				"OSO",
				"GIG",
				"OSO", 
				
				'O', Blocks.OBSIDIAN,
				'S', Items.SLIME_BALL,
				'G', Items.GLOWSTONE_DUST,
				'I', Items.ITEM_FRAME
			);
		}
	}
}
