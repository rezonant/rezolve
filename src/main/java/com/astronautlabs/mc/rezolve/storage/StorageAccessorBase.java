package com.astronautlabs.mc.rezolve.storage;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class StorageAccessorBase implements IStorageAccessor {
	@Override
	public List<ItemStack> readItems() {
		ArrayList<ItemStack> list = new ArrayList<>();
		this.readItems(list);
		return list;
	}

	@Override
	public List<ItemStack> readItems(String query) {
		ArrayList<ItemStack> list = new ArrayList<>();
		this.readItems(list);

		List<ItemStack> filteredResults = new ArrayList<>();

		for (ItemStack stack : list) {
			boolean keep = true;

			if (query != null && !"".equals(query)) {
				String name = stack.getDisplayName().getString(); // TODO: is this right? was an I18n before
				keep = (name.toLowerCase().contains(query.toLowerCase()));
			}

			if (keep)
				filteredResults.add(stack);
		}

		return filteredResults;
	}

	@Override
	public abstract void readItems(List<ItemStack> list);

	@Override
	public List<ItemStack> readItems(int offset, int size) {
		return readItems("", offset, size);
	}

	@Override
	public List<ItemStack> readItems(String query, int offset, int size) {
		List<ItemStack> list = this.readItems(query);

		if (offset >= list.size())
			return new ArrayList<>();

		return list.subList(offset, Math.min(list.size(), offset + size));
	}
}
