package com.astronautlabs.mc.rezolve.bundleBuilder;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;

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

	@Override
	public String getTooltipHint(ItemStack itemStack) {
		// TODO Auto-generated method stub
		return "I am a pattern!";
	}
}
