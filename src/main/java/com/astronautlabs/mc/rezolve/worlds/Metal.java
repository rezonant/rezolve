package com.astronautlabs.mc.rezolve.worlds;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.blocks.BlockBase;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.*;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = RezolveMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum Metal {
	COPPER("copper",
			new Properties().withCommonOre(7, 20, 300)),

	LEAD("lead",
			new Properties().withCommonOre(5, 20, 150)),

	TIN("tin",
			new Properties().withCommonOre(6, 20, 200));

	Metal(String name) {
		this.name = name;
	}

	Metal(String name, Properties properties) {
		this.name = name;
		properties.apply(this);
	}

	private String name;
	private float hardness = 3;
	private float resistance = 15;
	private StorageBlock storageBlock;
	private Ore ore;
	private DeepSlateOre deepSlateOre;
	private Ingot ingot;
	private Nugget nugget;
	private List<PlacementModifier> orePlacement;
	private boolean hasOre = false;

	private static class Properties {
		private List<Consumer<Metal>> configurers = new ArrayList<>();
		private boolean hasHardness = false;

		public Properties hardness(float hardness) {
			configurers.add(m -> m.hardness = hardness);
			hasHardness = true;
			return this;
		}

		public Properties resistance(float resistance) {
			configurers.add(m -> m.resistance = resistance);
			if (!hasHardness)
				configurers.add(m -> m.hardness = resistance / 5.0F);

			return this;
		}

		public Properties withRareOre(int chance, int minY, int maxY) {
			configurers.add(m -> m.hasOre = true);
			configurers.add(m -> m.orePlacement = List.of(
					RarityFilter.onAverageOnceEvery(chance),
					InSquarePlacement.spread(),
					HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(minY), VerticalAnchor.aboveBottom(maxY)),
					BiomeFilter.biome()
			));

			return this;
		}

		public Properties withCommonOre(int veinsPerChunk, int minY, int maxY) {
			configurers.add(m -> m.hasOre = true);
			configurers.add(m -> m.orePlacement = List.of(
					CountPlacement.of(veinsPerChunk),
					InSquarePlacement.spread(),
					HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(minY), VerticalAnchor.aboveBottom(maxY)),
					BiomeFilter.biome()
			));

			return this;
		}

		private void apply(Metal metal) {
			for (var configurer : configurers)
				configurer.accept(metal);
		}
	}

	public List<PlacementModifier> getOrePlacement() {
		return orePlacement;
	}

	public String getName() {
		return name;
	}

	public float getHardness() {
		return hardness;
	}

	public StorageBlock storageBlock() {
		return storageBlock != null ? storageBlock : (storageBlock = new StorageBlock());
	}

	public Ore ore() {
		return ore != null ? ore : (ore = new Ore());
	}

	public DeepSlateOre deepSlateOre() {
		return deepSlateOre != null ? deepSlateOre : (deepSlateOre = new DeepSlateOre());
	}

	public Ingot ingot() {
		return ingot != null ? ingot : (ingot = new Ingot());
	}

	public Nugget nugget() {
		return nugget != null ? nugget : (nugget = new Nugget());
	}

	public float getResistance() {
		return resistance;
	}

	public static Metal byName(String name) {
		return Arrays.stream(values()).filter(m -> Objects.equals(m.name, name)).findFirst().orElse(null);
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		for (var metal : values()) {
			event.getGenerator().addProvider(true, metal.new Recipes(event));
			event.getGenerator().addProvider(true, metal.new BlocksGenerator(event));
			event.getGenerator().addProvider(true, metal.new ItemsGenerator(event));
		}
	}

	private ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> overworldOreConfiguration;

	public ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> getOverworldOreConfiguration() {
		return overworldOreConfiguration;
	}

	@SubscribeEvent
	public static void handleRegisterEvent(RegisterEvent event) {
		RezolvePacket.init();

		if (event.getRegistryKey() == ForgeRegistries.Keys.BLOCKS) {
			for (var metal : values())  {
				event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(RezolveMod.ID, metal.getName() + "_block"), () -> metal.storageBlock());
				event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(RezolveMod.ID, metal.getName() + "_ore"), () -> metal.ore());
				event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(RezolveMod.ID, "deepslate_" + metal.getName() + "_ore"), () -> metal.deepSlateOre());
			}
		} else if (event.getRegistryKey() == ForgeRegistries.Keys.ITEMS) {
			for (var metal : values()) {
				event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.ID, metal.getName() + "_block"), () -> metal.storageBlock().item());

				if (metal.hasOre) {
					event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.ID, metal.getName() + "_ore"), () -> metal.ore().item());
					event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.ID, "deepslate_" + metal.getName() + "_ore"), () -> metal.deepSlateOre().item());
				}
				event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.ID, metal.getName() + "_ingot"), () -> metal.ingot());
				event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation(RezolveMod.ID, metal.getName() + "_nugget"), () -> metal.nugget());
			}
		} else if (event.getRegistryKey() == Registry.CONFIGURED_FEATURE_REGISTRY) {
			for (var metal : values()) {
				if (!metal.hasOre)
					continue;

				event.register(
						Registry.CONFIGURED_FEATURE_REGISTRY,
						RezolveMod.loc(metal.getName()),
						() -> metal.overworldOreConfiguration = new ConfiguredFeature<>(
								Feature.ORE,
								new OreConfiguration(List.of(
									OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, metal.ore().defaultBlockState()),
									OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, metal.deepSlateOre().defaultBlockState())
								), 7)
						)
				);
			}
		} else if (event.getRegistryKey() == Registry.PLACED_FEATURE_REGISTRY) {
			for (var metal : values()) {
				if (!metal.hasOre)
					continue;

				event.register(
						Registry.PLACED_FEATURE_REGISTRY,
						RezolveMod.loc(metal.getName()),
						() -> new PlacedFeature(
								new Holder.Direct<>(metal.overworldOreConfiguration),
								metal.orePlacement
						)
				);
			}
		}

	}

	@Override
	public String toString() {
		return String.format("Metal [%s]", this.name);
	}

	public class Block extends BlockBase {
		public Block(String stateName) {
			super(
					Properties.of(Material.METAL)
							.destroyTime(getHardness())
							.explosionResistance(getResistance())
			);

			this.stateName = stateName;
		}

		private String stateName;
		private Item item = new Item();

		public Metal getMetal() {
			return Metal.this;
		}

		@Override
		public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
			return getResistance();
		}

		public BlockItem item() {
			return item;
		}

		public class Item extends BlockItem {
			public Item() {
				super(Block.this, new Properties().tab(CreativeModeTab.TAB_MISC));
			}

			@Override
			public Component getName(ItemStack stack) {
				return Component.empty()
						.append(Component.translatable("rezolve.metals." + getMetal().getName()))
						.append(" ")
						.append(Component.translatable("rezolve.metal_forms." + stateName))
						;
			}
		}
	}

	public class Item extends ItemBase {
		public Item(String stateName) {
			super(new Properties());

			this.stateName = stateName;
		}

		private String stateName;

		public Metal getMetal() {
			return Metal.this;
		}

		@Override
		public Component getName(ItemStack stack) {
			return Component.empty()
					.append(Component.translatable("rezolve.metals."+getMetal().getName()))
					.append(" ")
					.append(Component.translatable("rezolve.metal_forms."+this.stateName))
					;
		}
	}

	public class Ingot extends Item {
		public Ingot() {
			super("ingot");
		}
	}

	public class StorageBlock extends Block {
		public StorageBlock() {
			super("block");
		}
	}

	public class Nugget extends Item {
		public Nugget() {
			super("nugget");
		}
	}

	public class Ore extends Block {
		public Ore() {
			super("ore");
		}
	}

	public class DeepSlateOre extends Block {
		public DeepSlateOre() {
			super("deepslate_ore");
		}
	}

	public class BlocksGenerator extends BlockStateProvider {

		public BlocksGenerator(GatherDataEvent event) {
			super(event.getGenerator(), RezolveMod.ID, event.getExistingFileHelper());
		}

		@Override
		protected void registerStatesAndModels() {
			var name = getName();

			blockModel(ore, "ore");
			blockModel(deepSlateOre, "deepslate","ore");
			blockModel(storageBlock, "block");
		}

		private void blockModel(Block block, String type) {
			blockModel(block, "", type);
		}

		private void blockModel(Block block, String prefix, String type) {
			if (!prefix.isEmpty())
				prefix = prefix + "_";

			var blockName = prefix + Metal.this.getName() + "_" + type;
			var metalFolder = "metals/" + Metal.this.getName() + "/";

			simpleBlock(block, models().cubeAll(blockName, RezolveMod.loc(metalFolder + blockName)));

			itemModels().getBuilder(blockName)
				.parent(new ModelFile.UncheckedModelFile(RezolveMod.loc("block/" + blockName)))
				;
		}

	}

	public class ItemsGenerator extends ItemModelProvider {
		public ItemsGenerator(GatherDataEvent event) {
			super(event.getGenerator(), RezolveMod.ID, event.getExistingFileHelper());
		}

		private void itemModel(String type) {
			var itemName = Metal.this.getName() + "_" + type;
			var metalFolder = "metals/" + Metal.this.getName() + "/";

			getBuilder(itemName)
					.parent(new ModelFile.UncheckedModelFile(RezolveMod.loc("item/standard_item")))
					.texture("layer0", RezolveMod.loc(metalFolder + itemName))
			;
		}

		@Override
		protected void registerModels() {
			itemModel("ingot");
			itemModel("nugget");
		}
	}

	public class Recipes extends RecipeProvider {
		public Recipes(GatherDataEvent event) {
			super(event.getGenerator());
		}

		@Override
		protected final void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
			ShapedRecipeBuilder.shaped(ingot, 1)
					.pattern("aaa")
					.pattern("aaa")
					.pattern("aaa")
					.define('a', nugget)
					.unlockedBy("has_ore", has(ingot))
					.save(consumer, RezolveMod.loc(name + "_ingot"));
			ShapedRecipeBuilder.shaped(storageBlock, 1)
					.pattern("aaa")
					.pattern("aaa")
					.pattern("aaa")
					.define('a', ingot)
					.unlockedBy("has_ingot", has(ingot))
					.save(consumer, RezolveMod.loc(name + "_block"));
			ShapelessRecipeBuilder.shapeless(nugget, 9)
					.requires(ingot)
					.unlockedBy("has_ingot", has(ingot))
					.save(consumer, RezolveMod.loc(name + "_nugget"));
			ShapelessRecipeBuilder.shapeless(ingot, 9)
					.requires(storageBlock)
					.unlockedBy("has_storage_block", has(storageBlock))
					.save(consumer, RezolveMod.loc(name + "_ingot_from_block"));
			SimpleCookingRecipeBuilder.smelting(Ingredient.of(ore.item()), ingot, 0.7F, 100)
					.unlockedBy("has_ore", has(ore))
					.save(consumer, RezolveMod.loc(name + "_ingot_from_ore"));
		}
	}
}
