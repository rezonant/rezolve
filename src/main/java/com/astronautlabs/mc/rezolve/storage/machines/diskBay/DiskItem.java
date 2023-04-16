package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RegistryId("disk")
public class DiskItem extends ItemBase implements ITooltipHint {

	public DiskItem() {
		super(new Properties());
	}

	int getSizeOfStack(ItemStack stack) {
		if (stack.getTag() == null || !stack.getTag().contains("size"))
			return -1;

		return stack.getTag().getInt("size");
	}

	public String formattedSize(ItemStack stack) {
		var capacity = determineSize(stack);
		if (capacity < 0)
			return "None";

		if (capacity >= (1024*1024)) {
			return String.format("%dM", capacity / (1024*1024));
		} else {
			return String.format("%dK", capacity / 1024);
		}
	}

	@Override
	public Component getName(ItemStack pStack) {
		return Component.empty()
				.append(Component.translatable("item.rezolve.item_disk"))
				.append(" (")
				.append(Component.literal(formattedSize(pStack)))
				.append(")");
	}

	public static int determineSize(ItemStack diskItem) {
		if (diskItem.getTag() == null || !diskItem.getTag().contains("size"))
			return 0;

		return (int)Math.pow(2, diskItem.getTag().getInt("size")) * 1024;
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
	public @Nullable CompoundTag getShareTag(ItemStack stack) {
		return new DiskAccessor(stack).getShareTag();
	}

	public ItemStack getSizedDisk(int diskSize) {
		return getSizedDisk(diskSize, 1);
	}

	public ItemStack getSizedDisk(int diskSize, int count) {
		var tag = new CompoundTag();
		tag.putInt("size", diskSize);
		var stack = new ItemStack(this, count);

		stack.setTag(tag);
		return stack;
	}

	@Override
	public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> subItems) {
		if (this.allowedIn(pCategory)) {
			subItems.add(getSizedDisk(0)); // 2^0 = 1K
			subItems.add(getSizedDisk(1)); // 2^1 = 2K
			subItems.add(getSizedDisk(2)); // 2^2 = 4K
			subItems.add(getSizedDisk(3)); // 2^3 = 8K
			subItems.add(getSizedDisk(4)); // 2^4 = 16K
			subItems.add(getSizedDisk(5)); // 2^5 = 32K
			subItems.add(getSizedDisk(6)); // 2^6 = 64K
			subItems.add(getSizedDisk(7)); // 2^7 = 128k
			subItems.add(getSizedDisk(8)); // 2^8 = 256k
			subItems.add(getSizedDisk(9)); // 2^9 = 512k
			subItems.add(getSizedDisk(10)); // 2^10 = 1024 = 1M
			subItems.add(getSizedDisk(11)); // 2^11 = 2048 = 2M
			subItems.add(getSizedDisk(12)); // 2^12 = 4096 = 4M
			subItems.add(getSizedDisk(13)); // 2^13 = 8192 = 8M
			subItems.add(getSizedDisk(14)); // 2^14 = 16384 = 16M
			subItems.add(getSizedDisk(15)); // 2^15 = 32768 = 32M
		}
	}

//	@Override
//	public void registerRecipes() {
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 0),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|0"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 1),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|1"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 2),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|2"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 3),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|3"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 4),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|4"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 5),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|5"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 6),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|6"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 7),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|7"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 8),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|8"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 9),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|9"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 10),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|10"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 11),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|11"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 12),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|12"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 13),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|13"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 14),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|14"
//		);
//
//		RecipeUtil.add(
//			new ItemStack(RezolveMod.DISK_ITEM, 1, 15),
//			"SgS",
//			"gig",
//			"SsS",
//
//			'S', "mc:slime_ball",
//			'g', "mc:gold_nugget",
//			'i', "item_machine_part|integrated_circuit",
//			's', "item_storage_part|15"
//		);
//
//
//	}
}
