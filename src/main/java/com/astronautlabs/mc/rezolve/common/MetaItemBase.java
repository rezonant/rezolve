package com.astronautlabs.mc.rezolve.common;

import java.util.List;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class MetaItemBase extends ItemBase {

	public MetaItemBase(String registryName) {
		super(registryName);

		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}
	
	/**
	 * Must be overridden in subclass of MetaItemBase. Subclass should fill list 'subItems' with 
	 * an item stack of each variant, each one with a different metadata value.
	 */
	@Override
	public abstract void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems);

	/**
	 * Must be overridden in subclass of MetaItemBase. Subclass should return a different unlocalized name per variant 
	 * as provided by getSubItems. This unlocalized name will determine the item model used to render the variant item
	 * as well as change the name of the item in game. Each name must be translated in each .lang file in resources.
	 * Use getUnlocalizedName() to get the generic unlocalized name to build from.
	 */
	@Override
	public abstract String getUnlocalizedName(ItemStack stack);
}