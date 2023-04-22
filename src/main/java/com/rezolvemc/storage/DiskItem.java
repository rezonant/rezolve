package com.rezolvemc.storage;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.ITooltipHint;
import com.rezolvemc.common.ItemBase;
import com.rezolvemc.storage.machines.diskBay.DiskAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DiskItem extends ItemBase implements ITooltipHint {

	public DiskItem(int size) {
		super(new Properties());
		this.size = size;
	}

	int size;

	public int getSize() {
		return size;
	}

	public String formattedSize() {
		var capacity = getCapacity();
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
				.append(Component.literal(formattedSize()))
				.append(")");
	}

	public int getCapacity() {
		return (int)Math.pow(2, size) * 1024;
	}

	public static int determineSize(ItemStack diskStack) {
		if (diskStack.getItem() instanceof DiskItem diskItem)
			return diskItem.getSize();

		return 0;
	}

	public static int determineCapacity(ItemStack diskStack) {
		if (diskStack.getItem() instanceof DiskItem diskItem)
			return diskItem.getCapacity();

		return 0;
	}

	public static ItemStack getSizedStack(int storageSize) {
		return getSizedStack(storageSize, 1);
	}
	public static ItemStack getSizedStack(int storageSize, int count) {
		return new ItemStack(sizes.get(storageSize), count);
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

	private static Map<Integer, DiskItem> sizes = new HashMap<>();

	private static DiskItem addSize(DiskItem item) {
		sizes.put(item.size, item);
		return item;
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		event.getGenerator().addProvider(true, new ItemsGenerator(event));
	}

	@SubscribeEvent
	public static void register(RegisterEvent event) {
		if (event.getRegistryKey() == ForgeRegistries.Keys.ITEMS) {
			for (int i = 0, max = 16; i < max; ++i) {
				var item = addSize(new DiskItem(i));
				event.register(ForgeRegistries.Keys.ITEMS, Rezolve.loc("disk_" + i), () -> item);
			}
		}
	}

	private static class ItemsGenerator extends ItemModelProvider {
		public ItemsGenerator(GatherDataEvent event) {
			super(event.getGenerator(), Rezolve.ID, event.getExistingFileHelper());
		}

		@Override
		protected void registerModels() {
			for (int i = 0, max = 16; i < max; ++i) {
				getBuilder("disk_" + i)
						.parent(new ModelFile.UncheckedModelFile(Rezolve.loc("item/standard_item")))
						.texture("layer0", Rezolve.loc("disks/size_" + i))
				;
			}
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
