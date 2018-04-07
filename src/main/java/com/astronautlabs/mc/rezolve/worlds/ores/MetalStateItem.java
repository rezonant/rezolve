package com.astronautlabs.mc.rezolve.worlds.ores;

import com.astronautlabs.mc.rezolve.common.MetaItemBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MetalStateItem extends MetaItemBase {
	protected String stateName;

	public MetalStateItem(String stateName) {
		super("item_metal_"+stateName);
		this.stateName = stateName;
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {

		for (int i = 0, max = Metal.all().size(); i < max; ++i)
			subItems.add(new ItemStack(this, 1, i));
	}

	public ItemStack getStackOf(Metal metal, int amount) {
		return new ItemStack(this, amount, Metal.indexOf(metal));
	}

	/**
	 * Get a 1-item stack of the given metal item.
	 * @param metal
	 * @return
	 */
	public ItemStack getStackOf(Metal metal) {
		return this.getStackOf(metal, 1);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		Metal metal = Metal.get(stack.getMetadata());

		if (metal == null)
			return super.getItemStackDisplayName(stack);

		return I18n.format("item.rezolve:metal_"+metal.getName()+".name") + " " + I18n.format("item.rezolve:"+this.stateName);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		Metal metal = Metal.get(stack.getMetadata());

		if (metal != null)
			return "item." + this.getRegistryName().getResourceDomain() + ":item_" + metal.getName()+"_"+this.stateName;
		else
			return this.getUnlocalizedName();
	}
}
