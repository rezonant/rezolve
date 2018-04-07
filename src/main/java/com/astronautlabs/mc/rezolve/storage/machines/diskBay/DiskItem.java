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

public class DiskItem extends MetaItemBase implements ITooltipHint {

	public DiskItem() {
		super("item_disk");
	}

	@Override
	public String getTooltipHint(ItemStack itemStack) {

		DiskAccessor.DiskStatus status = DiskAccessor.getDiskStatus(itemStack);

		if (status == null)
			return "This disk is corrupted.";

		if (status.spaceUsed > 0) {
			return String.format("%s / %s items stored (%s%%)", status.spaceUsed, status.size, (int) ((float) status.spaceUsed / status.size * 100.0f));
		} else {
			return "This disk is empty.";
		}
	}

	@Override
	public boolean getShareTag() {
		// Don't send our NBT to the client
		return false;
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) {
		return new DiskAccessor(stack).getShareTag();
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

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 0),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|0"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 1),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|1"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 2),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|2"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 3),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|3"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 4),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|4"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 5),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|5"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 6),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|6"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 7),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|7"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 8),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|8"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 9),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|9"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 10),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|10"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 11),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|11"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 12),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|12"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 13),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|13"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 14),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|14"
		);

		RecipeUtil.add(
			new ItemStack(RezolveMod.DISK_ITEM, 1, 15),
			"SgS",
			"gig",
			"SsS",

			'S', "mc:slime_ball",
			'g', "mc:gold_nugget",
			'i', "item_machine_part|integrated_circuit",
			's', "item_storage_part|15"
		);


	}
}
