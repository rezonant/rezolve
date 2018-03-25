package com.astronautlabs.mc.rezolve.storage;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * @deprecated
 */
public interface IItemStorage {
	int getTotalItems();
	int getTotalStacks();
	List<ItemStack> readItems(String query, int offset, int size);

	/**
	 * Give the items to the item storage.
	 * Returns a stack of items which could not be taken.
	 *
	 * @param toGive
	 * @param simulate
	 * @return
	 */
	ItemStack giveItemStack(ItemStack toGive, boolean simulate);

	/**
	 * Take the items from the item storage.
	 * Returns the stack of items which were successfully taken.
	 * @param toGive
	 * @param simulate
	 * @return
	 */
	ItemStack takeItemStack(ItemStack toGive, boolean simulate);
}
