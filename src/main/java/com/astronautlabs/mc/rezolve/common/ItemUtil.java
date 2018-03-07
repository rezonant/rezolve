package com.astronautlabs.mc.rezolve.common;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ItemUtil {
	public static boolean registered(String resourceName) {
		return Item.REGISTRY.getObject(new ResourceLocation(resourceName)) != null;
	}
}
