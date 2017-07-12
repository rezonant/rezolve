package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ItemBase;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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

		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {

			RezolveMod.addRecipe(new ItemStack(this), 
				"PSP",
				"GFG",
				"PSP", 
				
				'P', "item|enderio:itemAlloy|5",
				'S', "item|enderio:itemMaterial",
				'G', Items.GLOWSTONE_DUST,
				'F', "item|enderio:itemBasicFilterUpgrade"
			);
			
		} else {
			RezolveMod.addRecipe(new ItemStack(this), 
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
