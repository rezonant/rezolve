package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

@RegistryId("storage_part")
public class StoragePartItem extends ItemBase {

	public StoragePartItem() {
		super(new Properties());
	}

	public ItemStack getSizedStack(int storageSize) {
		return getSizedStack(storageSize, 1);

	}
	public ItemStack getSizedStack(int storageSize, int count) {
		var tag = new CompoundTag();
		tag.putInt("type", storageSize);

		return new ItemStack(this, 1, tag);
	}

	@Override
	public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
		super.fillItemCategory(pCategory, pItems);
		pItems.add(getSizedStack(0)); // 2^0 = 1K
		pItems.add(getSizedStack(1)); // 2^1 = 2K
		pItems.add(getSizedStack(2)); // 2^2 = 4K
		pItems.add(getSizedStack(3)); // 2^3 = 8K
		pItems.add(getSizedStack(4)); // 2^4 = 16K
		pItems.add(getSizedStack(5)); // 2^5 = 32K
		pItems.add(getSizedStack(6)); // 2^6 = 64K
		pItems.add(getSizedStack(7)); // 2^7 = 128k
		pItems.add(getSizedStack(8)); // 2^8 = 256k
		pItems.add(getSizedStack(9)); // 2^9 = 512k
		pItems.add(getSizedStack(10)); // 2^10 = 1024 = 1M
		pItems.add(getSizedStack(11)); // 2^11 = 2048 = 2M
		pItems.add(getSizedStack(12)); // 2^12 = 4096 = 4M
		pItems.add(getSizedStack(13)); // 2^13 = 8192 = 8M
		pItems.add(getSizedStack(14)); // 2^14 = 16384 = 16M
		pItems.add(getSizedStack(15)); // 2^15 = 32768 = 32M
	}

//	@Override
//	public void registerRecipes() {
//		RecipeUtil.add( // 1K
//			new ItemStack(RezolveRegistry.item(StoragePartItem.class), 1, 0),
//
//			" p ",
//			"eie",
//			" c ",
//
//			'p', "item_bundle_pattern|blank",
//			'e', "mc:ender_pearl",
//			'i', "item_machine_part|integrated_circuit",
//			'c', "mc:chest"
//		);
//
//		RecipeUtil.add(	// 2K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 1),
//
//			"iIi",
//			"0R0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'I', "mc:iron_ingot",
//			'0', "item_storage_part|0",
//			'R', "mc:redstone"
//		);
//
//		RecipeUtil.add(	// 4K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 2),
//
//			"iIi",
//			"0R0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'I', "mc:gold_ingot",
//			'0', "item_storage_part|1",
//			'R', "mc:redstone"
//		);
//
//		RecipeUtil.add(	// 8K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 3),
//
//			"iIi",
//			"0D0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'I', "mc:gold_ingot",
//			'0', "item_storage_part|2",
//			'D', "mc:diamond"
//		);
//
//
//		RecipeUtil.add(	// 16K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 4),
//
//			"iDi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'D', "mc:diamond",
//			'E', "mc:ender_pearl",
//			'0', "item_storage_part|3"
//		);
//
//		RecipeUtil.add(	// 32K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 5),
//
//			"iDi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'D', "mc:diamond",
//			'E', "mc:ender_eye",
//			'0', "item_storage_part|4"
//		);
//
//		RecipeUtil.add(	// 64K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 6),
//
//			"iEi",
//			"0S0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:ender_eye",
//			'S', "mc:nether_star",
//			'0', "item_storage_part|5"
//		);
//
//		RecipeUtil.add(	// 128K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 7),
//
//			"iSi",
//			"0D0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'D', "mc:diamond_block",
//			'S', "mc:nether_star",
//			'0', "item_storage_part|6"
//		);
//
//		RecipeUtil.add(	// 256K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 8),
//
//			"iSi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:emerald",
//			'S', "mc:nether_star",
//			'0', "item_storage_part|7"
//		);
//
//
//		RecipeUtil.add(	// 512K
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 9),
//
//			"iSi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:emerald_block",
//			'S', "mc:nether_star",
//			'0', "item_storage_part|8"
//		);
//
//
//		RecipeUtil.add(	// 1M
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 10),
//
//			"iBi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:emerald_block",
//			'B', "mc:beacon",
//			'0', "item_storage_part|9"
//		);
//
//		RecipeUtil.add(	// 2M
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 11),
//
//			"iBi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:dragon_egg",
//			'B', "mc:beacon",
//			'0', "item_storage_part|10"
//		);
//
//		RecipeUtil.add(	// 4M
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 12),
//
//			"iBi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:dragon_egg",
//			'B', "mc:beacon",
//			'0', "item_storage_part|11"
//		);
//
//		RecipeUtil.add(	// 8M
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 13),
//
//			"iBi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:dragon_egg",
//			'B', "mc:beacon",
//			'0', "item_storage_part|12"
//		);
//
//		RecipeUtil.add(	// 16M
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 14),
//
//			"iBi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:dragon_egg",
//			'B', "mc:beacon",
//			'0', "item_storage_part|13"
//		);
//
//		RecipeUtil.add(	// 32M
//			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 15),
//
//			"iBi",
//			"0E0",
//			"i0i",
//
//			'i', "item_machine_part|integrated_circuit",
//			'E', "mc:dragon_egg",
//			'B', "mc:beacon",
//			'0', "item_storage_part|14"
//		);
//
//
//	}
}
