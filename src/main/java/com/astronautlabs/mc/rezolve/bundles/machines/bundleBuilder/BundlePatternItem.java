package com.astronautlabs.mc.rezolve.bundles.machines.bundleBuilder;

import java.util.ArrayList;
import java.util.List;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.MetaItemBase;
import com.astronautlabs.mc.rezolve.core.inventory.VirtualInventory;

import com.astronautlabs.mc.rezolve.util.ItemUtil;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import com.astronautlabs.mc.rezolve.worlds.ores.Metal;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class BundlePatternItem extends MetaItemBase implements ITooltipHint {
	public BundlePatternItem() {
		super("item_bundle_pattern");
	}

	@Override
	public void registerRecipes() {

		RecipeUtil.add(
			this.blank(),
			"tGt",
			"GCG",
			"tIt",

			't', RezolveMod.METAL_NUGGET_ITEM.getStackOf(Metal.TIN),
			'I', "mc:item_frame",
			'G', "mc:glass",
			'C', RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.COPPER)
		);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			String localizedName = super.getItemStackDisplayName(stack);
			
			ArrayList<String> props = new ArrayList<String>();
			
			if (nbt.hasKey("Name")) {
				props.add(nbt.getString("Name"));
			}
			
			if (nbt.hasKey("Color")) {
				int dye = nbt.getInteger("Color");
				String colorName = ItemUtil.getColorName(dye);
				props.add(colorName);
			}
			
			if (props.size() > 0)
				return localizedName + " ("+String.join(", ", props)+")";
			else
				return localizedName + " (Configured)";
		}
		
		return super.getItemStackDisplayName(stack);
	}

	private VirtualInventory dummyInventory = new VirtualInventory();
	
	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return RezolveMod.BUNDLE_ITEM.describeContents(itemStack);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, 1));
	}

	public boolean isBlank(ItemStack stack) {
		return stack.getMetadata() == 1;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() == 1)
			return this.getUnlocalizedName()+"_blank";
		else
			return this.getUnlocalizedName();
	}

	public ItemStack blank() {
		return this.blank(1);
	}
	
	public ItemStack blank(int size) {
		return new ItemStack(this, size, 1);
	}
}
