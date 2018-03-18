package com.astronautlabs.mc.rezolve.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemUtil {
	public static boolean registered(String resourceName) {
		return Item.REGISTRY.getObject(new ResourceLocation(resourceName)) != null;
	}

	public static boolean isDye(Item item) {
		return "minecraft:dye".equals(item.getRegistryName().toString());
	}

	public static String getColorName(int dye) {
		if (dye < 0 || dye >= DYE_NAMES.length)
			return "";

		return DYE_NAMES[dye];
	}

	public static final String[] DYES = new String[] { "black", "red", "green", "brown", "blue", "purple", "cyan",
		"light_gray", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white" };

	public static final String[] DYE_NAMES = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan",
		"Light Gray", "Gray", "Pink", "Lime", "Yellow", "Light Blue", "Magenta", "Orange", "White" };

	public static boolean areStacksSame(ItemStack stackA, ItemStack stackB) {
		if (stackA == stackB)
			return true;

		if (stackA == null || stackB == null)
			return false;

		return (stackA.isItemEqual(stackB) && ItemStack.areItemStackTagsEqual(stackA, stackB));
	}

}
