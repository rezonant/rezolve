package com.rezolvemc.parts;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.ItemBase;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StoragePartItem extends ItemBase {

	public StoragePartItem(int size) {
		super(new Properties());
		this.size = size;
	}

	int size;

	public static ItemStack getSizedStack(int storageSize) {
		return getSizedStack(storageSize, 1);
	}
	public static ItemStack getSizedStack(int storageSize, int count) {
		return new ItemStack(sizes.get(storageSize), count);
	}
	private static Map<Integer, StoragePartItem> sizes = new HashMap<>();

	private static StoragePartItem addSize(StoragePartItem item) {
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
				var item = addSize(new StoragePartItem(i));
				event.register(ForgeRegistries.Keys.ITEMS, Rezolve.loc("storage_part_" + i), () -> item);
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
				getBuilder("storage_part_" + i)
						.parent(new ModelFile.UncheckedModelFile(Rezolve.loc("item/standard_item")))
						.texture("layer0", Rezolve.loc("storage_parts/size_" + i))
				;
			}
		}
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
