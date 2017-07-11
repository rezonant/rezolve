package com.astronautlabs.mc.rezolve.bundleBuilder;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.RezolveNBT;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BundlePatternItem extends ItemBase implements ITooltipHint {
	public BundlePatternItem() {
		super("item_bundle_pattern");
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
				String colorName = RezolveMod.instance().getColorName(dye);
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

		NBTTagCompound nbt = itemStack.getTagCompound();
		String name = null;
		
		if (nbt == null || !nbt.hasKey("Items")) {
			return "Combines multiple items for automation. See Bundler/Unbundler";
		}
		
		dummyInventory.clear();
		RezolveNBT.readInventory(nbt, dummyInventory);
		ArrayList<String> itemStrings = new ArrayList<String>();
		
		for (ItemStack stack : dummyInventory.getStacks()) {
			Item item = stack.getItem();
			itemStrings.add(stack.stackSize+" "+item.getItemStackDisplayName(stack));
		}
		
		return String.join("\n", itemStrings);
	}
}
