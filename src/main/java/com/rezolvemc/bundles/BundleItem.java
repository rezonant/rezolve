package com.rezolvemc.bundles;

import java.util.*;

import com.rezolvemc.common.inventory.VirtualInventory;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.util.RezolveTagUtil;
import com.rezolvemc.common.ITooltipHint;
import com.rezolvemc.common.ItemBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@RegistryId("bundle")
public class BundleItem extends ItemBase implements ITooltipHint {
	public BundleItem() {
		super(
				new Item.Properties()
						.stacksTo(1)
		);
	}

	public static final Map<String, BundleItem> COLORS = new HashMap<>();

//	static {
//		for (int i = 0, max = RezolveMod.DYES.length; i < max; ++i)
//			COLORS.put(RezolveMod.DYES[i], new BundleItem(RezolveMod.DYES[i]));
//	}

	public static BundleItem withColor(String color) {
		return COLORS.get(color);
	}

	@Override
	public Component getName(ItemStack pStack) {
		Component name = super.getName(pStack);
		CompoundTag nbt = pStack.getTag();

		if (nbt == null || !nbt.contains("Items")) {
			return name;
		}

		if (nbt != null && nbt.contains("Name")) {

			String customName = nbt.getString("Name");

			if (!"".equals(name))
				return Component.empty()
						.append(name)
						.append(" (")
						.append(customName)
						.append(")")
				;
		}


		return name;
	}

	/**
	 * Get the total count of items involved in a bundle or bundle pattern, including 
	 * checking into all sub-bundles
	 * @param bundleOrPattern
	 * @return
	 */
	public static int countBundleItems(ItemStack bundleOrPattern) {
		if (!bundleOrPattern.hasTag())
			return 0;
		
		CompoundTag nbt = bundleOrPattern.getTag();
		VirtualInventory vinv = new VirtualInventory();
		RezolveTagUtil.readInventory(nbt, vinv);
		
		int itemCount = 0;
		
		for (ItemStack stack : vinv.getStacks()) {
			if (stack == null)
				continue;
			
			if (stack.getItem() == RezolveRegistry.item(BundleItem.class))
				itemCount += countBundleItems(stack);
			
			itemCount += stack.getCount();
		}
		
		return itemCount;
	}
	
	public static int getBundleCost(ItemStack bundleOrPattern) {
		
		int cost = 1000;
		int itemCount = countBundleItems(bundleOrPattern);
		int bundleDepth = getBundleDepth(bundleOrPattern);
		
		if (itemCount > 9)
			cost += itemCount * 100;
		
		if (bundleDepth > 0) {
			cost *= Math.pow(2, bundleDepth);
		}
		
		return cost;
	}
	
	public static Collection<ItemStack> getItemsFromBundle(ItemStack bundleOrPattern) {
		
		if (!bundleOrPattern.hasTag())
			return new ArrayList<ItemStack>();
		
		CompoundTag nbt = bundleOrPattern.getTag();
		VirtualInventory vinv = new VirtualInventory();
		
		RezolveTagUtil.readInventory(nbt, vinv);
		return vinv.getStacks();
	}
	
	/**
	 * Get the "depth" of a bundle or bundle pattern, which is how many layers of bundles are involved.
	 * @param bundleOrPattern
	 * @return
	 */
	public static int getBundleDepth(ItemStack bundleOrPattern) {
		int subdepth = 0;
		
		for (ItemStack stack : getItemsFromBundle(bundleOrPattern)) {
			if (stack == null)
				continue;
			
			if (stack.getItem() == RezolveRegistry.item(BundleItem.class)) {
				int thisSubDepth = getBundleDepth(stack);
				subdepth = Math.max(subdepth, thisSubDepth);
			}
		}
		
		return 1 + subdepth;
	}
	
	VirtualInventory dummyInventory = new VirtualInventory();
	
	public String describeContents(ItemStack bundleOrPattern) {
		return describeContents(bundleOrPattern, 0);
	}
	
	public String describeContents(ItemStack bundleOrPattern, int depth) {
		CompoundTag nbt = bundleOrPattern.getTag();
		
		if (nbt == null || !nbt.contains("Items")) {
			return "Combines multiple items for automation. See Bundler/Unbundler";
		}
		
		ArrayList<String> itemStrings = new ArrayList<String>();
		String prefix = "";
		
		for (int i = 0; i < depth; ++i)
			prefix += "  ";
		
		for (ItemStack stack : getItemsFromBundle(bundleOrPattern)) {
			Item item = stack.getItem();
			itemStrings.add(prefix + stack.getCount()+" "+item.getName(stack));
			if (item == RezolveRegistry.item(BundleItem.class)) {
				itemStrings.add(describeContents(stack, depth + 1));
			}
		}
		
		return String.join("\n", itemStrings);
	}
	
	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return this.describeContents(itemStack);
	}

	public ItemStack withContents(int count, ItemStack ...stacks) {
		ItemStack result = new ItemStack(this, count);
		
		VirtualInventory vinv = new VirtualInventory();
		
		int slot = 0;
		for (ItemStack stack : stacks) {
			vinv.setItem(slot, stack);
			++slot;
		}
		
		CompoundTag nbt = new CompoundTag();
		RezolveTagUtil.writeInventory(nbt, vinv);
		
		result.setTag(nbt);
		return result;
	}
}
