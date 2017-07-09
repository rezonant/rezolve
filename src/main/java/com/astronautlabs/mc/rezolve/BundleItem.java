package com.astronautlabs.mc.rezolve;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.common.BundlerNBT;
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
			
			return localizedName + " (" + name + ")";
		}
		
		
		return localizedName;
	}
	
	VirtualInventory dummyInventory = new VirtualInventory();
	
	@Override
	public String getTooltipHint(ItemStack itemStack) {
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		String name = null;
		
		if (nbt == null || !nbt.hasKey("Items")) {
			return "Combines multiple items for automation. See Bundler/Unbundler";
		}
		
		dummyInventory.clear();
		BundlerNBT.readInventory(nbt, dummyInventory);
		ArrayList<String> itemStrings = new ArrayList<String>();
		
		for (ItemStack stack : dummyInventory.getStacks()) {
			Item item = stack.getItem();
			itemStrings.add(item.getItemStackDisplayName(stack));
		}
		
		return String.join("\n", itemStrings);
	}
}
