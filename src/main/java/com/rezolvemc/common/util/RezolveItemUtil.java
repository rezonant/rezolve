package com.rezolvemc.common.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RezolveItemUtil {
    public static String getName(ItemStack stack) {
        return I18n.get(stack.getItem().getName(stack).getString());
    }

    public static String getName(Item item) {
        return I18n.get(item.getName(new ItemStack(item, 1)).getString());
    }
}
