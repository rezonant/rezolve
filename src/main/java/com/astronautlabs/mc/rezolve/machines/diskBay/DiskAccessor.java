package com.astronautlabs.mc.rezolve.machines.diskBay;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DiskAccessor {
	public DiskAccessor(ItemStack disk) {
		this.sizeMetadata = disk.getMetadata();
		this.nbt = disk.getTagCompound();
		this.size = determineSize(disk);

		if (this.nbt == null)
			this.nbt = new NBTTagCompound();

		this.readFromNBT();
	}

	public static final String PROP_DISK = "disk";
	public static final String PROP_DISK_CONTENTS = "disk.toc";
	public static final String PROP_COUNT = "disk.c";
	public static final String PROP_ITEM = "disk.i";
	public static final String PROP_VARIANTS = "disk.v";

	public static int determineSize(ItemStack diskItem) {
		return (int)Math.pow(2, diskItem.getMetadata()) * 1000;
	}

	public static DiskStatus getDiskStatus(ItemStack diskItem) {
		DiskStatus diskStatus = new DiskStatus();

		diskStatus.disk = diskItem;
		diskStatus.size = determineSize(diskItem);
		diskStatus.spaceUsed = 0;

		NBTTagCompound itemTag = diskItem.getTagCompound();
		if (itemTag != null) {
			NBTTagCompound disk = itemTag.getCompoundTag(PROP_DISK);
			diskStatus.spaceUsed = disk.getInteger(PROP_COUNT);
		}

		return diskStatus;
	}

	public List<ItemStack> readItems() {
		return readItems("");
	}

	public List<ItemStack> readItems(String query) {
		ArrayList<ItemStack> list = new ArrayList<>();
		this.readItems(list);

		List<ItemStack> filteredResults = new ArrayList<ItemStack>();

		for (ItemStack stack : list) {
			boolean keep = true;

			if (query != null && !"".equals(query)) {
				String name = I18n.format(stack.getDisplayName());
				keep = (name.toLowerCase().contains(query.toLowerCase()));
			}

			if (keep)
				filteredResults.add(stack);
		}

		return filteredResults;
	}

	public void readItems(List<ItemStack> list) {
		for (HashMap.Entry<String, TypeContents> entry : this.types.entrySet()) {
			TypeContents typeContents = entry.getValue();
			typeContents.readItems(list);
		}
	}

	public List<ItemStack> readItems(int offset, int size) {
		return readItems("", offset, size);
	}

	public List<ItemStack> readItems(String query, int offset, int size) {
		List<ItemStack> list = this.readItems(query);

		if (offset >= list.size())
			return new ArrayList<>();

		return list.subList(offset, Math.min(list.size(), offset + size));
	}

	public static class DiskStatus {
		public ItemStack disk;
		public int size;
		public int spaceUsed;
	}

	private int sizeMetadata;
	private int typeCount;
	private int size;
	private int spaceUsed;
	private NBTTagCompound nbt;
	private HashMap<String, TypeContents> types;
	private boolean dirty = false;

	private void readFromNBT() {
		this.dirty = false;
		this.types = new HashMap<>();
		if (this.nbt == null)
			return;

		NBTTagCompound disk = this.nbt.getCompoundTag(PROP_DISK);
		NBTTagList list = disk.getTagList(PROP_DISK_CONTENTS, Constants.NBT.TAG_COMPOUND);

		this.types = new HashMap<>();

		int totalCount = 0;

		for (int i = 0, max = list.tagCount(); i < max; ++i) {
			NBTTagCompound typeContentsTag = list.getCompoundTagAt(i);
			TypeContents typeContents = new TypeContents();
			typeContents.readFromNBT(typeContentsTag);

			if (typeContents.count <= 0)
				continue;

			String hash = ItemStackUtil.hashOfStack(typeContents.template);

			this.types.put(hash, typeContents);

			totalCount += typeContents.count;
			typeCount += typeContents.variants.size();
		}

		this.spaceUsed = totalCount;

		if (this.types.size() == 0) {

			Random r = new Random();

			this.giveItemStack(new ItemStack(Items.DIAMOND, 20 + r.nextInt(100), 0), false);
			this.giveItemStack(new ItemStack(Items.GOLD_INGOT, 20 + r.nextInt(100), 0), false);
			this.giveItemStack(new ItemStack(Items.IRON_INGOT, 20 + r.nextInt(100), 0), false);

			this.writeToNBT();
		}
	}

	public void writeToNBT() {

		NBTTagList list = new NBTTagList();
		int totalCount = 0;

		for (HashMap.Entry<String, TypeContents> entry : this.types.entrySet()) {
			NBTTagCompound typeContentsTag = new NBTTagCompound();
			TypeContents typeContents = entry.getValue();

			if (typeContents.count <= 0)
				continue;

			typeContents.writeToNBT(typeContentsTag);
			list.appendTag(typeContentsTag);
			totalCount += typeContents.count;
		}

		NBTTagCompound disk = new NBTTagCompound();
		disk.setTag(PROP_DISK_CONTENTS, list);
		disk.setInteger(PROP_COUNT, totalCount);

		this.nbt.setTag(PROP_DISK, disk);
		this.dirty = false;
	}

	public ItemStack simplifyItemStack(ItemStack stack) {
		return new ItemStack(stack.getItem(), 1, stack.getMetadata());
	}

	public TypeContents bucketFor(ItemStack stack) {
		return this.bucketFor(stack, false);
	}

	public DiskAccessor.VariantContents variantFor(TypeContents bucket, ItemStack stack) {
		return this.variantFor(bucket, stack, false);
	}

	public VariantContents variantFor(TypeContents bucket, ItemStack stack, boolean create) {
		ItemStack singleItem = ItemStackUtil.getSingleItem(stack);
		String hash = ItemStackUtil.hashOfStack(singleItem);

		if (!bucket.variants.containsKey(hash)) {
			if (!create)
				return null;

			// create a variant and hook it in.
			// if it doesnt get populated before we save/load from NBT, the
			// bucket will be discarded.

			VariantContents variant = new VariantContents();
			variant.template = singleItem;
			variant.count = 0;

			bucket.variants.put(hash, variant);
			return variant;
		}

		return bucket.variants.get(hash);
	}

	public TypeContents bucketFor(ItemStack stack, boolean create) {
		ItemStack simpleItem = this.simplifyItemStack(stack);
		String hash = ItemStackUtil.hashOfStack(simpleItem);

		if (!this.types.containsKey(hash)) {
			if (!create)
				return null;

			TypeContents typeContents = new TypeContents();
			typeContents.template = simpleItem;
			typeContents.count = 0;

			this.types.put(hash, typeContents);
			return typeContents;
		}

		return this.types.get(hash);
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
	public ItemStack giveItemStack(ItemStack stack, boolean simulate) {

		// Shortcut: We have no space, we take no items.

		if (this.spaceUsed >= this.size)
			return stack;

		this.dirty = true;

		TypeContents bucket = bucketFor(stack,true);
		VariantContents variant = variantFor(bucket, stack, true);

		int availableSpace = this.size - this.spaceUsed;
		int takenItems = Math.min(stack.stackSize, availableSpace);

		if (takenItems < 0)
			takenItems = 0;

		if (takenItems == 0)
			return stack;

		int remainingItems = stack.stackSize - takenItems;

		if (!simulate) {
			if (variant.count == 0 && takenItems > 0) {
				this.typeCount += 1;
			}

			variant.count += takenItems;
			bucket.count += takenItems;
		}
		this.spaceUsed += takenItems;

		return ItemStackUtil.getStackSized(stack, remainingItems);
	}

	/**
	 * Attempts to remove items from this disk. Returns the items which were
	 * removed, if any.
	 * @param stack
	 * @param simulate
	 * @return
	 */
	public ItemStack takeItemStack(ItemStack stack, boolean simulate) {

		this.dirty = true;

		TypeContents bucket = bucketFor(stack);
		VariantContents variant = this.variantFor(bucket, stack, false);

		if (variant == null || variant.count <= 0)
			return ItemStackUtil.getEmptyStack(stack);

		int takeCount = (int)Math.min(stack.stackSize, variant.count);

		if (!simulate) {
			if (variant.count > 0 && takeCount >= variant.count) {
				this.typeCount -= 1;
			}

			bucket.count -= takeCount;
			variant.count -= takeCount;

			this.cleanupBuckets(bucket, variant);
		}

		return ItemStackUtil.getStackSized(stack, takeCount);
	}

	private void cleanupBuckets(TypeContents bucket, VariantContents variant) {
		if (variant.count <= 0)
			bucket.variants.remove(ItemStackUtil.hashOfStack(variant.template));

		if (bucket.count <= 0)
			this.types.remove(ItemStackUtil.hashOfStack(bucket.template));
	}

	public void clear() {
		nbt.removeTag(PROP_DISK_CONTENTS);
	}

	public int count(ItemStack stack) {

		TypeContents bucket = this.bucketFor(stack);
		VariantContents variant = this.variantFor(bucket, stack);

		if (variant == null)
			return 0;

		return variant.count;
	}

	public int getTotalItems() {
		return this.spaceUsed;
	}

	public int getTotalStacks() {
		return this.typeCount;
	}

	public int getSize() {
		return this.size;
	}

	/**
	 * Get the disk's current item stack. This encodes all of the contents of the disk.
	 *
	 * @return
	 */
	public ItemStack getItemStack() {
		if (this.dirty)
			this.writeToNBT();

		ItemStack stack = new ItemStack(RezolveMod.DISK_ITEM, 1, this.sizeMetadata);
		stack.setTagCompound(this.nbt);

		return stack;
	}

	public NBTTagCompound getShareTag() {
		NBTTagCompound shareable = new NBTTagCompound();
		NBTTagCompound disk = new NBTTagCompound();

		disk.setInteger(PROP_COUNT, this.spaceUsed);
		shareable.setTag(PROP_DISK, disk);

		return shareable;
	}

	public static class TypeContents {
		public ItemStack template;
		public int count;

		public HashMap<String, VariantContents> variants = new HashMap<>();

		public int getCount(ItemStack item) {
			if (item.stackSize != 1) {
				NBTTagCompound itemTag = item.getTagCompound();
				item = new ItemStack(item.getItem(), 1, item.getMetadata());
				item.setTagCompound(itemTag);
			}


			String hash = ItemStackUtil.hashOfStack(item);

			if (this.variants.containsKey(hash)) {
				VariantContents bucket = this.variants.get(hash);
				return bucket.count;
			}

			return 0;
		}

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger(PROP_COUNT, this.count);
			NBTTagCompound itemStackNBT = new NBTTagCompound();
			this.template.writeToNBT(itemStackNBT);
			nbt.setTag(PROP_ITEM, itemStackNBT);

			NBTTagList variants = new NBTTagList();

			for (HashMap.Entry<String, VariantContents> entry : this.variants.entrySet()) {
				VariantContents variant = entry.getValue();

				if (variant.count <= 0)
					continue;

				NBTTagCompound tag = new NBTTagCompound();
				variant.writeToNBT(tag);

				variants.appendTag(tag);
			}

			nbt.setTag(PROP_VARIANTS, variants);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			NBTTagCompound itemStackNBT = nbt.getCompoundTag(PROP_ITEM);
			this.template = ItemStack.loadItemStackFromNBT(itemStackNBT);
			this.count = nbt.getInteger(PROP_COUNT);

			NBTTagList variants = nbt.getTagList(PROP_VARIANTS, Constants.NBT.TAG_COMPOUND);

			for (int i = 0, max = variants.tagCount(); i < max; ++i) {
				NBTTagCompound variant = variants.getCompoundTagAt(i);
				VariantContents contents = new VariantContents();
				contents.readFromNBT(variant);

				if (contents.count <= 0)
					continue;

				String hash = ItemStackUtil.hashOfStack(contents.template);
				this.variants.put(hash, contents);
			}
		}

		public void readItems(List<ItemStack> list) {
			for (HashMap.Entry<String, VariantContents> entry : this.variants.entrySet()) {
				VariantContents variant = entry.getValue();
				variant.readItems(list);
			}
		}
	}

	public static class VariantContents {
		public ItemStack template;
		public int count;

		public void writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger(PROP_COUNT, this.count);

			NBTTagCompound stackTag = new NBTTagCompound();
			template.writeToNBT(stackTag);
			nbt.setTag(PROP_ITEM, stackTag);
		}

		public void readFromNBT(NBTTagCompound nbt) {
			this.count = nbt.getInteger(PROP_COUNT);

			NBTTagCompound stackTag = nbt.getCompoundTag(PROP_ITEM);
			this.template = ItemStack.loadItemStackFromNBT(stackTag);
		}

		public void readItems(List<ItemStack> list) {
			list.add(ItemStackUtil.getStackSized(this.template, count));
		}
	}
}
