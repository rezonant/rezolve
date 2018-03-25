package com.astronautlabs.mc.rezolve.storage;

import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiplexedStorageAccessor extends StorageAccessorBase implements IStorageAccessor {

	public MultiplexedStorageAccessor() {

	}

	public MultiplexedStorageAccessor(List<IStorageAccessor> accessors) {
		this.accessors = accessors;
	}

	protected List<IStorageAccessor> accessors = new ArrayList<>();


	public List<IStorageAccessor> getAccessors() {
		return this.accessors;
	}

	public void setAccessors(List<IStorageAccessor> list) {
		this.accessors = list;
	}

	/**
	 * Use this to prepare before a query.
	 */
	protected void onQuery() {

	}

	@Override
	public void readItems(List<ItemStack> list) {

		this.onQuery();

		HashMap<String, ItemStack> itemMap = new HashMap<>();

		for (IStorageAccessor diskAccessor : this.accessors) {
			for (ItemStack stack : diskAccessor.readItems()) {
				String stackKey = ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack));

				if (itemMap.containsKey(stackKey)) {
					ItemStack existingStack = itemMap.get(stackKey);
					existingStack.stackSize += stack.stackSize;
				} else {
					ItemStack ownedStack = ItemStackUtil.cloneStack(stack);
					itemMap.put(stackKey, ownedStack);
					list.add(ownedStack);
				}
			}
		}
	}

	@Override
	public ItemStack giveItemStack(ItemStack stack, String hashLocator, boolean simulate) {

		this.onQuery();

		ItemStack toGive = stack;

		for (IStorageAccessor accessor : this.accessors) {
			toGive = accessor.giveItemStack(toGive, hashLocator, simulate);

			if (toGive.stackSize <= 0)
				break;
		}

		return toGive;
	}

	@Override
	public ItemStack takeItemStack(ItemStack stack, String hashLocator, boolean simulate) {
		this.onQuery();

		ItemStack desired = ItemStackUtil.cloneStack(stack);
		ItemStack found = ItemStackUtil.getEmptyStack(stack);

		for (IStorageAccessor accessor : this.accessors) {
			ItemStack taken = accessor.takeItemStack(desired, hashLocator, simulate);

			if (found.stackSize == 0)
				found = taken;
			else
				found.stackSize += taken.stackSize;

			desired.stackSize -= taken.stackSize;

			if (desired.stackSize <= 0)
				break;
		}

		return found;
	}

	@Override
	public void clear() {
		this.onQuery();

		for (IStorageAccessor accessor : this.accessors)
			accessor.clear();
	}

	@Override
	public int count(ItemStack stack, String hashLocator) {
		this.onQuery();

		int count = 0;

		for (IStorageAccessor accessor : this.accessors)
			count += accessor.count(stack, hashLocator);

		return count;
	}

	@Override
	public int getTotalItems() {
		this.onQuery();

		int count = 0;

		for (IStorageAccessor accessor : this.accessors)
			count += accessor.getTotalItems();

		return count;
	}

	@Override
	public int getTotalStacks() {
		this.onQuery();

		// TODO: oh crap, this is super inefficient
		return this.readItems().size();
	}

	@Override
	public int getSize() {
		this.onQuery();

		int count = 0;

		for (IStorageAccessor accessor : this.accessors)
			count += accessor.getSize();

		return count;
	}
}
