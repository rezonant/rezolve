package com.astronautlabs.mc.rezolve.storage;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IStorageAccessor {
	List<ItemStack> readItems();

	List<ItemStack> readItems(String query);

	void readItems(List<ItemStack> list);

	List<ItemStack> readItems(int offset, int size);

	List<ItemStack> readItems(String query, int offset, int size);

	/**
	 * Add a stack of items to the disk (or simulate it).
	 * Returns the remaining items that didn't fit, or a zero item
	 * stack.
	 *
	 * @param stack
	 * @param simulate
	 * @return
	 */
	ItemStack giveItemStack(ItemStack stack, String hashLocator, boolean simulate);

	/**
	 * Attempts to remove items from this disk. Returns the items which were
	 * removed, if any.
	 * @param stack
	 * @param simulate
	 * @return
	 */
	ItemStack takeItemStack(ItemStack stack, String hashLocator, boolean simulate);

	void clear();

	int count(ItemStack stack, String hashLocator);

	int getTotalItems();

	int getTotalStacks();

	int getSize();
}
