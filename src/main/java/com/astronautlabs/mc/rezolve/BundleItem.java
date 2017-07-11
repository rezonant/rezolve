package com.astronautlabs.mc.rezolve;

import java.util.ArrayList;
import java.util.Collection;

import com.astronautlabs.mc.rezolve.common.RezolveNBT;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class BundleItem extends ItemBase implements ITooltipHint {
	public BundleItem(String color) {
		super("item_bundle_"+color);
	}
	
	public BundleItem() {
		super("item_bundle");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		
		String localizedName = super.getItemStackDisplayName(stack);
		NBTTagCompound nbt = stack.getTagCompound();
		
		if (nbt == null || !nbt.hasKey("Items")) {
			return localizedName;
		}
		
		if (nbt != null && nbt.hasKey("Name")) {

			String name = nbt.getString("Name");
			
			if (!"".equals(name))
				return localizedName + " (" + name + ")";
		}
		
		
		return localizedName;
	}

	/**
	 * Get the total count of items involved in a bundle or bundle pattern, including 
	 * checking into all sub-bundles
	 * @param bundleOrPattern
	 * @return
	 */
	public static int countBundleItems(ItemStack bundleOrPattern) {
		if (!bundleOrPattern.hasTagCompound())
			return 0;
		
		NBTTagCompound nbt = bundleOrPattern.getTagCompound();
		VirtualInventory vinv = new VirtualInventory();
		RezolveNBT.readInventory(nbt, vinv);
		
		int itemCount = 0;
		
		for (ItemStack stack : vinv.getStacks()) {
			if (stack == null)
				continue;
			
			if (RezolveMod.instance().isBundleItem(stack.getItem()))
				itemCount += countBundleItems(stack);
			
			itemCount += stack.stackSize;
		}
		
		return itemCount;
	}
	
	public static int getBundleCost(ItemStack bundleOrPattern) {
		
		int cost = 1000;
		int itemCount = countBundleItems(bundleOrPattern);
		int bundleDepth = getBundleDepth(bundleOrPattern);
		
		if (itemCount > 9)
			cost += itemCount * 100;
		
		if (bundleDepth > 0) {
			cost *= Math.pow(2, bundleDepth);
		}
		
		return cost;
	}
	
	public static Collection<ItemStack> getItemsFromBundle(ItemStack bundleOrPattern) {
		
		if (!bundleOrPattern.hasTagCompound())
			return new ArrayList<ItemStack>();
		
		NBTTagCompound nbt = bundleOrPattern.getTagCompound();
		VirtualInventory vinv = new VirtualInventory();
		
		RezolveNBT.readInventory(nbt, vinv);
		return vinv.getStacks();
	}
	
	/**
	 * Get the "depth" of a bundle or bundle pattern, which is how many layers of bundles are involved.
	 * @param bundleOrPattern
	 * @return
	 */
	public static int getBundleDepth(ItemStack bundleOrPattern) {
		int subdepth = 0;
		
		for (ItemStack stack : getItemsFromBundle(bundleOrPattern)) {
			if (stack == null)
				continue;
			
			if (RezolveMod.instance().isBundleItem(stack.getItem())) {
				int thisSubDepth = getBundleDepth(stack);
				subdepth = Math.max(subdepth, thisSubDepth);
			}
		}
		
		return 1 + subdepth;
	}
	
	VirtualInventory dummyInventory = new VirtualInventory();
	
	public String describeContents(ItemStack bundleOrPattern) {
		return describeContents(bundleOrPattern, 0);
	}
	
	public String describeContents(ItemStack bundleOrPattern, int depth) {
		NBTTagCompound nbt = bundleOrPattern.getTagCompound();
		
		if (nbt == null || !nbt.hasKey("Items")) {
			return "Combines multiple items for automation. See Bundler/Unbundler";
		}
		
		ArrayList<String> itemStrings = new ArrayList<String>();
		String prefix = "";
		
		for (int i = 0; i < depth; ++i)
			prefix += "  ";
		
		for (ItemStack stack : getItemsFromBundle(bundleOrPattern)) {
			Item item = stack.getItem();
			itemStrings.add(prefix + stack.stackSize+" "+item.getItemStackDisplayName(stack));
			if (RezolveMod.instance().isBundleItem(item)) {
				itemStrings.add(describeContents(stack, depth + 1));
			}
		}
		
		return String.join("\n", itemStrings);
	}
	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return this.describeContents(itemStack);
	}
}
