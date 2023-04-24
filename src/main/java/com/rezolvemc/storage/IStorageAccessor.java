package com.rezolvemc.storage;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface IStorageAccessor {
	default List<ItemStack> readItems() {
		return readItems("", 0, -1);
	}

	default List<ItemStack> readItems(String query) {
		return readItems(query, 0, -1);
	}

	void readItems(List<ItemStack> list);

	default List<ItemStack> readItems(int offset, int size) {
		return readItems("", offset, size);
	}

	default List<ItemStack> readItems(String query, int offset, int size) {
		List<ItemStack> list = new ArrayList<>() {
			@Override
			public boolean add(ItemStack itemStack) {
				if (!itemStack.getDisplayName().getString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
					return false;

				return super.add(itemStack);
			}
		};

		readItems(list);


		if (size < 0)
			size = list.size();

		if (offset >= list.size())
			return new ArrayList<>();

		size = Math.min(list.size() - offset, size);

		if (offset > 0)
			list = list.subList(offset, offset+size);

		return list;
	}

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
