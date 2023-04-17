package com.astronautlabs.mc.rezolve.worlds;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.blocks.BlockBase;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = RezolveMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum Metal {
	LEAD("lead", new Properties()
			.withCommonOre(60, 40, 120)
			.hardness(1)
			.withVeinSize(5)
			.needsIronTool()
	),

	COBALT("cobalt", new Properties()
			.withRareOre(30, 0, 100)
			.hardness(3)
			.withVeinSize(8)
			.needsDiamondTool()
	),

	TIN("tin", new Properties()
			.withCommonOre(70, 60, 500)
			.hardness(2)
			.withVeinSize(12)
			.needsStoneTool()
	),

	SILVER("silver", new Properties()
			.withRareOre(60, 40, 500)
			.hardness(3)
			.withVeinSize(20)
	),

	;

	private int veinSize = 7;

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
	private List<TagKey<Block>> blockTags = new ArrayList<>();

	{
		// All metal blocks will inherit these tags
		blockTags.add(BlockTags.SNAPS_GOAT_HORN);
	}

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

		public Properties withTag(TagKey<Block> tag) {
			configurers.add(m -> m.blockTags.add(tag));
			return this;
		}

		public Properties withForgeTag(String forgeTag) {
			configurers.add(m -> m.blockTags.add(BlockTags.create(new ResourceLocation("forge", forgeTag))));
			return this;
		}

		public Properties needsDiamondTool() {
			return withTag(BlockTags.NEEDS_DIAMOND_TOOL);
		}


		public Properties needsIronTool() {
			return withTag(BlockTags.NEEDS_IRON_TOOL);
		}

		public Properties needsStoneTool() {
			return withTag(BlockTags.NEEDS_STONE_TOOL);
		}

		public Properties needsWoodTool() {
			return withForgeTag("needs_wood_tool");
		}

		public Properties needsGoldTool() {
			return withForgeTag("needs_gold_tool");
		}

		public Properties needsNetheriteTool() {
			return withForgeTag("needs_netherite_tool");
		}

		public Properties withVeinSize(int veinSize) {
			configurers.add(m -> m.veinSize = veinSize);
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
		var generator = event.getGenerator();
		for (var metal : values()) {
			generator.addProvider(true, metal.new Recipes(event));
			generator.addProvider(true, metal.new BlocksGenerator(event));
			generator.addProvider(true, metal.new ItemsGenerator(event));
			generator.addProvider(true, metal.new BiomeModifiersGenerator(event));
		}
	}

	private ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> overworldOreConfiguration;
	private PlacedFeature overworldOrePlacedFeature;

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
								), metal.veinSize)
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
						() -> metal.overworldOrePlacedFeature = new PlacedFeature(
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

	public class MetalBlock extends BlockBase {
		public MetalBlock(String stateName) {
			this(stateName, Properties.of(Material.METAL));
		}

		public MetalBlock(String stateName, Properties properties) {
			super(
					properties
							.destroyTime(getHardness())
							.explosionResistance(getResistance())
			);

			this.stateName = stateName;
		}

		{
			applyTags(tagger -> {
				for (var tag : getMetal().blockTags)
					tagger.tag(tag);
			});
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
				super(MetalBlock.this, new Properties().tab(RezolveMod.CREATIVE_MODE_TAB));
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

	public class StorageBlock extends MetalBlock {
		public StorageBlock() {
			super("block");
		}

		{
			applyTags(tagger -> {
				tagger.tag("forge:storage_blocks/" + getMetal().getName());
			});
		}
	}

	public class Nugget extends Item {
		public Nugget() {
			super("nugget");
		}
	}

	public class Ore extends MetalBlock {
		public Ore() {
			super("ore", Properties.of(Material.METAL)
					.requiresCorrectToolForDrops()
			);
		}

		{
			applyTags(tagger -> {
				tagger.tag("forge:ores_in_ground/stone");
				tagger.tag("forge:ores/" + getMetal().getName());
				tagger.tag("forge:ore_rates/singular");
			});
		}
	}

	public class DeepSlateOre extends MetalBlock {
		public DeepSlateOre() {
			super("deepslate_ore");
		}
	}

	public class BiomeModifiersGenerator implements DataProvider {
		BiomeModifiersGenerator(GatherDataEvent event) {
			generator = event.getGenerator();
		}

		private DataGenerator generator;

		@Override
		public void run(CachedOutput pOutput) throws IOException {
			var obj = new JsonObject();
			obj.addProperty("type", "forge:add_features");
			obj.addProperty("biomes", "#minecraft:is_overworld");
			obj.addProperty("features", "rezolve:" + Metal.this.getName());
			obj.addProperty("step", "underground_ores");

			Path mainOutput = generator.getOutputFolder();
			String pathSuffix = "data/rezolve/forge/biome_modifier/add_" + Metal.this.getName() + ".json";
			Path outputPath = mainOutput.resolve(pathSuffix);
			DataProvider.saveStable(pOutput, obj, outputPath);
		}

		@Override
		public String getName() {
			return "Biome Modifiers: rezolve";
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

		private void blockModel(MetalBlock block, String type) {
			blockModel(block, "", type);
		}

		private void blockModel(MetalBlock block, String prefix, String type) {
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