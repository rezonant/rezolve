package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.MetaItemBase;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class StoragePartItem extends MetaItemBase {

	public StoragePartItem() {
		super("item_storage_part");
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0)); // 2^0 = 1K
		subItems.add(new ItemStack(itemIn, 1, 1)); // 2^1 = 2K
		subItems.add(new ItemStack(itemIn, 1, 2)); // 2^2 = 4K
		subItems.add(new ItemStack(itemIn, 1, 3)); // 2^3 = 8K
		subItems.add(new ItemStack(itemIn, 1, 4)); // 2^4 = 16K
		subItems.add(new ItemStack(itemIn, 1, 5)); // 2^5 = 32K
		subItems.add(new ItemStack(itemIn, 1, 6)); // 2^6 = 64K
		subItems.add(new ItemStack(itemIn, 1, 7)); // 2^7 = 128k
		subItems.add(new ItemStack(itemIn, 1, 8)); // 2^8 = 256k
		subItems.add(new ItemStack(itemIn, 1, 9)); // 2^9 = 512k
		subItems.add(new ItemStack(itemIn, 1, 10)); // 2^10 = 1024 = 1M
		subItems.add(new ItemStack(itemIn, 1, 11)); // 2^11 = 2048 = 2M
		subItems.add(new ItemStack(itemIn, 1, 12)); // 2^12 = 4096 = 4M
		subItems.add(new ItemStack(itemIn, 1, 13)); // 2^13 = 8192 = 8M
		subItems.add(new ItemStack(itemIn, 1, 14)); // 2^14 = 16384 = 16M
		subItems.add(new ItemStack(itemIn, 1, 15)); // 2^15 = 32768 = 32M
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName()+"_"+stack.getMetadata();
	}

	@Override
	public void registerRecipes() {
		RecipeUtil.add( // 1K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 0),

			" p ",
			"eie",
			" c ",

			'p', "item_bundle_pattern|blank",
			'e', "mc:ender_pearl",
			'i', "item_machine_part|integrated_circuit",
			'c', "mc:chest"
		);

		RecipeUtil.add(	// 2K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 1),

			"iIi",
			"0R0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'I', "mc:iron_ingot",
			'0', "item_storage_part|0",
			'R', "mc:redstone"
		);

		RecipeUtil.add(	// 4K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 2),

			"iIi",
			"0R0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'I', "mc:gold_ingot",
			'0', "item_storage_part|1",
			'R', "mc:redstone"
		);

		RecipeUtil.add(	// 8K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 3),

			"iIi",
			"0D0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'I', "mc:gold_ingot",
			'0', "item_storage_part|2",
			'D', "mc:diamond"
		);


		RecipeUtil.add(	// 16K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 4),

			"iDi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'D', "mc:diamond",
			'E', "mc:ender_pearl",
			'0', "item_storage_part|3"
		);

		RecipeUtil.add(	// 32K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 5),

			"iDi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'D', "mc:diamond",
			'E', "mc:ender_eye",
			'0', "item_storage_part|4"
		);

		RecipeUtil.add(	// 64K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 6),

			"iEi",
			"0S0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:ender_eye",
			'S', "mc:nether_star",
			'0', "item_storage_part|5"
		);

		RecipeUtil.add(	// 128K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 7),

			"iSi",
			"0D0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'D', "mc:diamond_block",
			'S', "mc:nether_star",
			'0', "item_storage_part|6"
		);

		RecipeUtil.add(	// 256K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 8),

			"iSi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:emerald",
			'S', "mc:nether_star",
			'0', "item_storage_part|7"
		);


		RecipeUtil.add(	// 512K
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 9),

			"iSi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:emerald_block",
			'S', "mc:nether_star",
			'0', "item_storage_part|8"
		);


		RecipeUtil.add(	// 1M
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 10),

			"iBi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:emerald_block",
			'B', "mc:beacon",
			'0', "item_storage_part|9"
		);

		RecipeUtil.add(	// 2M
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 11),

			"iBi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:dragon_egg",
			'B', "mc:beacon",
			'0', "item_storage_part|10"
		);

		RecipeUtil.add(	// 4M
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 12),

			"iBi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:dragon_egg",
			'B', "mc:beacon",
			'0', "item_storage_part|11"
		);

		RecipeUtil.add(	// 8M
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 13),

			"iBi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:dragon_egg",
			'B', "mc:beacon",
			'0', "item_storage_part|12"
		);

		RecipeUtil.add(	// 16M
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 14),

			"iBi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:dragon_egg",
			'B', "mc:beacon",
			'0', "item_storage_part|13"
		);

		RecipeUtil.add(	// 32M
			new ItemStack(RezolveMod.STORAGE_PART_ITEM, 1, 15),

			"iBi",
			"0E0",
			"i0i",

			'i', "item_machine_part|integrated_circuit",
			'E', "mc:dragon_egg",
			'B', "mc:beacon",
			'0', "item_storage_part|14"
		);


	}
}
