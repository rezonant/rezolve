package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.storage.DiskItem;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class DiskAccessor implements IStorageAccessor {

	public DiskAccessor(ItemStack disk) {
		this(disk, null);
	}

	public DiskAccessor(ItemStack disk, IListener listener) {
		this.nbt = disk.getTag();
		this.listener = listener;

		if (this.nbt == null)
			this.nbt = new CompoundTag();

		this.sizeMetadata = DiskItem.determineSize(disk);
		this.size = DiskItem.determineCapacity(disk);
		this.readFromNBT();
	}

	public static boolean accepts(ItemStack stack) {
		return stack != null && stack.getItem() instanceof DiskItem;
	}

	public interface IListener {
		void diskChanged(ItemStack disk);
	}

	private IListener listener = null;

	public static final String PROP_DISK = "disk";
	public static final String PROP_DISK_CONTENTS = "disk.toc";
	public static final String PROP_COUNT = "disk.c";
	public static final String PROP_ITEM = "disk.i";
	public static final String PROP_VARIANTS = "disk.v";

	public static DiskStatus getDiskStatus(ItemStack diskItem) {
		DiskStatus diskStatus = new DiskStatus();

		diskStatus.disk = diskItem;
		diskStatus.size = DiskItem.determineCapacity(diskItem);
		diskStatus.spaceUsed = 0;

		CompoundTag itemTag = diskItem.getTag();
		if (itemTag != null) {
			CompoundTag disk = itemTag.getCompound(PROP_DISK);
			diskStatus.spaceUsed = disk.getInt(PROP_COUNT);
		}

		return diskStatus;
	}

	@Override
	public List<ItemStack> readItems() {
		return readItems("");
	}

	@Override
	public List<ItemStack> readItems(String query) {
		ArrayList<ItemStack> list = new ArrayList<>();
		this.readItems(list);

		List<ItemStack> filteredResults = new ArrayList<ItemStack>();

		for (ItemStack stack : list) {
			boolean keep = true;

			if (query != null && !"".equals(query)) {
				String name = stack.getDisplayName().getString();
				keep = (name.toLowerCase().contains(query.toLowerCase()));
			}

			if (keep)
				filteredResults.add(stack);
		}

		return filteredResults;
	}

	@Override
	public void readItems(List<ItemStack> list) {
		for (HashMap.Entry<String, TypeContents> entry : this.types.entrySet()) {
			TypeContents typeContents = entry.getValue();
			typeContents.readItems(list);
		}
	}

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

	public static class DiskStatus {
		public ItemStack disk;
		public int size;
		public int spaceUsed;
	}

	private int sizeMetadata;
	private int typeCount;
	private int size;
	private int spaceUsed;
	private CompoundTag nbt;
	private HashMap<String, TypeContents> types;
	private boolean dirty = false;

	private void readFromNBT() {
		this.dirty = false;
		this.types = new HashMap<>();
		if (this.nbt == null)
			return;

		CompoundTag disk = this.nbt.getCompound(PROP_DISK);
		ListTag list = disk.getList(PROP_DISK_CONTENTS, Tag.TAG_COMPOUND);

		this.types = new HashMap<>();

		int totalCount = 0;

		for (int i = 0, max = list.size(); i < max; ++i) {
			CompoundTag typeContentsTag = list.getCompound(i);
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

			this.giveItemStack(new ItemStack(Items.DIAMOND, 20 + r.nextInt(100)), null, false);
			this.giveItemStack(new ItemStack(Items.GOLD_INGOT, 20 + r.nextInt(100)), null, false);
			this.giveItemStack(new ItemStack(Items.IRON_INGOT, 20 + r.nextInt(100)), null, false);

			this.writeToNBT();
		}
	}

	private boolean hasChanges = false;

	public boolean hasChanges() {
		return this.hasChanges;
	}

	public void acknowledgeChanges() {
		this.hasChanges = false;
	}

	public void writeToNBT() {

		ListTag list = new ListTag();
		int totalCount = 0;

		for (HashMap.Entry<String, TypeContents> entry : this.types.entrySet()) {
			CompoundTag typeContentsTag = new CompoundTag();
			TypeContents typeContents = entry.getValue();

			if (typeContents.count <= 0)
				continue;

			typeContents.writeToNBT(typeContentsTag);
			list.add(typeContentsTag);
			totalCount += typeContents.count;
		}

		CompoundTag disk = new CompoundTag();
		disk.put(PROP_DISK_CONTENTS, list);
		disk.putInt(PROP_COUNT, totalCount);

		this.nbt.put(PROP_DISK, disk);
		this.dirty = false;
	}

	public ItemStack simplifyItemStack(ItemStack stack) {
		if (stack == null)
			return null;

		return new ItemStack(stack.getItem(), 1);
	}

	public TypeContents bucketFor(ItemStack stack) {
		return this.bucketFor(stack, false);
	}

	public DiskAccessor.VariantContents variantFor(TypeContents bucket, ItemStack stack, String hash) {
		return this.variantFor(bucket, stack, hash, false);
	}

	public VariantContents variantFor(TypeContents bucket, ItemStack stack, String hash, boolean create) {

		if (bucket == null)
			return null;

		ItemStack singleItem = ItemStackUtil.getSingleItem(stack);

		if (hash == null)
			hash = ItemStackUtil.hashOfStack(singleItem);

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
		if (stack == null)
			return null;

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
	@Override
	public ItemStack giveItemStack(ItemStack stack, String hashLocator, boolean simulate) {

		if (stack == null)
			return null;

		// Shortcut: We have no space, we take no items.

		if (this.spaceUsed >= this.size)
			return stack;

		TypeContents bucket = bucketFor(stack,true);
		VariantContents variant = variantFor(bucket, stack, hashLocator, true);

		int availableSpace = this.size - this.spaceUsed;
		int takenItems = Math.min(stack.getCount(), availableSpace);

		if (takenItems < 0)
			takenItems = 0;

		if (takenItems == 0)
			return stack;

		int remainingItems = stack.getCount() - takenItems;

		if (!simulate) {
			this.dirty = true;
			this.hasChanges = true;

			if (variant.count == 0 && takenItems > 0) {
				this.typeCount += 1;
			}

			variant.count += takenItems;
			bucket.count += takenItems;
			this.spaceUsed += takenItems;
			this.notifyListener();
		}

		return ItemStackUtil.getStackSized(stack, remainingItems);
	}

	/**
	 * Attempts to remove items from this disk. Returns the items which were
	 * removed, if any.
	 * @param stack
	 * @param simulate
	 * @return
	 */
	@Override
	public ItemStack takeItemStack(ItemStack stack, String hashLocator, boolean simulate) {

		if (stack == null)
			return null;

		TypeContents bucket = bucketFor(stack, false);

		if (bucket == null)
			return ItemStackUtil.getEmptyStack(stack);

		VariantContents variant = this.variantFor(bucket, stack, hashLocator, false);

		if (variant == null || variant.count <= 0)
			return ItemStackUtil.getEmptyStack(stack);

		int takeCount = (int)Math.min(stack.getCount(), variant.count);
		ItemStack pureStack = ItemStackUtil.cloneStack(variant.template);

		if (!simulate) {
			this.dirty = true;
			this.hasChanges = true;

			if (variant.count > 0 && takeCount >= variant.count) {
				this.typeCount -= 1;
			}

			bucket.count -= takeCount;
			variant.count -= takeCount;
			this.spaceUsed -= takeCount;
			this.cleanupBuckets(bucket, variant);
			this.notifyListener();
		}


		return ItemStackUtil.getStackSized(pureStack, takeCount);
	}

	private void notifyListener() {
		if (this.listener == null)
			return;

		this.listener.diskChanged(this.getItemStack());
	}

	private void cleanupBuckets(TypeContents bucket, VariantContents variant) {
		if (variant.count <= 0)
			bucket.variants.remove(ItemStackUtil.hashOfStack(variant.template));

		if (bucket.count <= 0)
			this.types.remove(ItemStackUtil.hashOfStack(bucket.template));
	}

	@Override
	public void clear() {
		nbt.remove(PROP_DISK_CONTENTS);
		this.dirty = true;
		this.hasChanges = true;
	}

	@Override
	public int count(ItemStack stack, String hashLocator) {

		TypeContents bucket = this.bucketFor(stack);
		VariantContents variant = this.variantFor(bucket, stack, hashLocator);

		if (variant == null)
			return 0;

		return variant.count;
	}

	@Override
	public int getTotalItems() {
		return this.spaceUsed;
	}

	@Override
	public int getTotalStacks() {
		return this.typeCount;
	}

	@Override
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

		ItemStack stack = DiskItem.getSizedStack(this.sizeMetadata);
		stack.setTag(this.nbt);

		return stack;
	}

	public CompoundTag getShareTag() {
		CompoundTag shareable = new CompoundTag();
		CompoundTag disk = new CompoundTag();

		disk.putInt(PROP_COUNT, this.spaceUsed);
		shareable.put(PROP_DISK, disk);

		return shareable;
	}

	public static class TypeContents {
		public ItemStack template;
		public int count;

		public HashMap<String, VariantContents> variants = new HashMap<>();

		public int getCount(ItemStack item) {
			if (item.getCount() != 1) {
				item = item.copy();
				item.setCount(1);
			}


			String hash = ItemStackUtil.hashOfStack(item);

			if (this.variants.containsKey(hash)) {
				VariantContents bucket = this.variants.get(hash);
				return bucket.count;
			}

			return 0;
		}

		public void writeToNBT(CompoundTag nbt) {
			nbt.putInt(PROP_COUNT, this.count);
			CompoundTag itemStackNBT = this.template.serializeNBT();
			nbt.put(PROP_ITEM, itemStackNBT);

			ListTag variants = new ListTag();

			for (HashMap.Entry<String, VariantContents> entry : this.variants.entrySet()) {
				VariantContents variant = entry.getValue();

				if (variant.count <= 0)
					continue;

				CompoundTag tag = new CompoundTag();
				variant.writeToNBT(tag);

				variants.add(tag);
			}

			nbt.put(PROP_VARIANTS, variants);
		}

		public void readFromNBT(CompoundTag nbt) {
			CompoundTag itemStackNBT = nbt.getCompound(PROP_ITEM);
			this.template = ItemStack.of(itemStackNBT);
			this.count = nbt.getInt(PROP_COUNT);

			ListTag variants = nbt.getList(PROP_VARIANTS, Tag.TAG_COMPOUND);

			for (int i = 0, max = variants.size(); i < max; ++i) {
				CompoundTag variant = variants.getCompound(i);
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

		public void writeToNBT(CompoundTag nbt) {
			nbt.putInt(PROP_COUNT, this.count);
			nbt.put(PROP_ITEM, template.serializeNBT());
		}

		public void readFromNBT(CompoundTag nbt) {
			this.count = nbt.getInt(PROP_COUNT);
			CompoundTag stackTag = nbt.getCompound(PROP_ITEM);
			this.template = ItemStack.of(stackTag);
		}

		public void readItems(List<ItemStack> list) {
			list.add(ItemStackUtil.getStackSized(this.template, count));
		}
	}
}
