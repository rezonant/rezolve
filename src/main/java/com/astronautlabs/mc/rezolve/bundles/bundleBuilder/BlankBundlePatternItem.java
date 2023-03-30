package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ItemBase;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class BlankBundlePatternItem extends ItemBase {
	public static final String ID = "item_bundle_pattern_blank";

	public BlankBundlePatternItem(Properties properties) {
		super(properties);
	}

//	@Override
//	public void registerRecipes() {
//
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//
//			RezolveMod.addRecipe(new ItemStack(this),
//				"PSP",
//				"GFG",
//				"PSP",
//
//				'P', "item|enderio:itemAlloy|5",
//				'S', "item|enderio:itemMaterial",
//				'G', Items.GLOWSTONE_DUST,
//				'F', "item|enderio:itemBasicFilterUpgrade"
//			);
//
//		} else {
//			RezolveMod.addRecipe(new ItemStack(this),
//				"OSO",
//				"GIG",
//				"OSO",
//
//				'O', Blocks.OBSIDIAN,
//				'S', Items.SLIME_BALL,
//				'G', Items.GLOWSTONE_DUST,
//				'I', Items.ITEM_FRAME
//			);
//		}
//	}
}
