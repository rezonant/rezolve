package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.core.ConfigProperty;
import com.astronautlabs.mc.rezolve.core.ModBase;
import com.astronautlabs.mc.rezolve.network.cable.EthernetCableBlock;
import com.astronautlabs.mc.rezolve.parts.MachineFrameBlock;
import com.astronautlabs.mc.rezolve.parts.MachinePart;
import com.astronautlabs.mc.rezolve.parts.MachinePartItem;
import com.astronautlabs.mc.rezolve.storage.gui.*;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.StoragePartItem;
import com.astronautlabs.mc.rezolve.storage.machines.storageMonitor.StorageMonitorBlock;
import com.astronautlabs.mc.rezolve.storage.machines.storageShell.StorageShellBlock;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import com.astronautlabs.mc.rezolve.worlds.cities.CityBiome;
import com.astronautlabs.mc.rezolve.worlds.cities.CityGenerator;
import com.astronautlabs.mc.rezolve.worlds.cities.CityMapGen;
import com.astronautlabs.mc.rezolve.core.inventory.GhostSlotUpdateMessageHandler;
import com.astronautlabs.mc.rezolve.bundles.machines.bundleBuilder.BundleBuilderBlock;
import com.astronautlabs.mc.rezolve.bundles.machines.bundleBuilder.BundlePatternItem;
import com.astronautlabs.mc.rezolve.bundles.machines.bundler.BundlerBlock;
import com.astronautlabs.mc.rezolve.network.machines.databaseServer.DatabaseServerBlock;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskBayBlock;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskItem;
import com.astronautlabs.mc.rezolve.storage.machines.diskManipulator.*;
import com.astronautlabs.mc.rezolve.network.machines.remoteShell.RemoteShellBlock;
import com.astronautlabs.mc.rezolve.network.machines.securityServer.SecurityServerBlock;
import com.astronautlabs.mc.rezolve.bundles.machines.unbundler.UnbundlerBlock;
import com.astronautlabs.mc.rezolve.mobs.dragon.DragonUpdateMessageHandler;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import com.astronautlabs.mc.rezolve.mobs.dragon.RenderDragon;
import com.astronautlabs.mc.rezolve.terrain.StonelessWorldType;
import com.astronautlabs.mc.rezolve.worlds.cities.TownBiome;
import com.astronautlabs.mc.rezolve.worlds.ores.*;
import mezz.jei.api.IJeiRuntime;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = RezolveMod.MODID, version = RezolveMod.VERSION, name = "Rezolve", dependencies = "after:Waila;after:EnderIO")
public class RezolveMod extends ModBase {

	public RezolveMod() {
		_instance = this;
	}

	public static final String MODID = "rezolve";
	public static final String VERSION = "1.0";

	public static final Logger logger = LogManager.getLogger(RezolveMod.MODID);
	private static RezolveMod _instance = null;
	public static RezolveMod instance() { return _instance; }

	// Blocks
	public static final BundlerBlock BUNDLER_BLOCK = new BundlerBlock();
	public static final UnbundlerBlock UNBUNDLER_BLOCK = new UnbundlerBlock();
	public static final BundleBuilderBlock BUNDLE_BUILDER_BLOCK = new BundleBuilderBlock();
	public static final EthernetCableBlock ETHERNET_CABLE_BLOCK = new EthernetCableBlock();
	public static final RemoteShellBlock REMOTE_SHELL_BLOCK = new RemoteShellBlock();
	public static final DatabaseServerBlock DATABASE_SERVER_BLOCK = new DatabaseServerBlock();
	public static final SecurityServerBlock SECURITY_SERVER_BLOCK = new SecurityServerBlock();
	public static final DiskBayBlock DISK_BAY_BLOCK = new DiskBayBlock();
	public static final DiskManipulatorBlock DISK_MANIPULATOR_BLOCK = new DiskManipulatorBlock();
	public static final StorageShellBlock STORAGE_SHELL_BLOCK = new StorageShellBlock();
	public static final StorageMonitorBlock STORAGE_MONITOR_BLOCK = new StorageMonitorBlock();
	public static final MachineFrameBlock MACHINE_FRAME_BLOCK = new MachineFrameBlock();

	// Metals
	public static final MetalOreBlock METAL_ORE_BLOCK = new MetalOreBlock();
	public static final MetalBlock METAL_BLOCK = new MetalBlock();
	public static final MetalIngotItem METAL_INGOT_ITEM = new MetalIngotItem();
	public static final MetalNuggetItem METAL_NUGGET_ITEM = new MetalNuggetItem();

	// Items
	public static final BundlePatternItem BUNDLE_PATTERN_ITEM = new BundlePatternItem();
	public static final DiskItem DISK_ITEM = new DiskItem();
	public static final MachinePartItem MACHINE_PART_ITEM = new MachinePartItem();
	public static final StoragePartItem STORAGE_PART_ITEM = new StoragePartItem();
	public static final BundleItem BUNDLE_ITEM = new BundleItem();

	@SidedProxy(clientSide = "com.astronautlabs.mc.rezolve.ClientProxy", serverSide = "com.astronautlabs.mc.rezolve.ServerProxy")
	public static CommonProxy proxy;

	public static final Biome TOWN_BIOME = new TownBiome();
	public static final CityBiome CITY_BIOME = new CityBiome();
	public static final CityGenerator CITY_GENERATOR = new CityGenerator();

	private CityMapGen cityMapGen;
	private OreGenerator oreGenerator;

	@ConfigProperty(comment = "Make new stone (diorite, etc) usable for crafting stone tools")
	protected boolean makeNewStoneUseful = false;

	@ConfigProperty(comment = "Improved death messages")
	protected boolean youDed = true;

	@ConfigProperty(comment = "Enable the City biome.")
	protected boolean enableCities = true;

	@ConfigProperty(comment = "Enable the Town biome.")
	protected boolean enableTowns = true;

	@ConfigProperty(comment = "Allow dragons to spawn naturally in the world.")
	protected boolean enableDragons = true;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		super.preinit(event);

		this.cityMapGen = new CityMapGen();
		this.oreGenerator = new OreGenerator();

		proxy.init(this);

		this.registerWaila();
	}

	private void registerWaila() {
		FMLInterModComms.sendMessage("Waila", "register", "com.astronautlabs.mc.rezolve.waila.WailaCompat.load");
	}

	@Override
	public void register() {
		this.registerMachineParts();
		this.registerMetals();
		super.register();
	}

	/**
	 * Register blocks!
	 */
	@Override
	protected void registerBlocks() {
		super.registerBlocks();

		this.registerItemBlock(RezolveMod.BUNDLER_BLOCK);
		this.registerItemBlock(RezolveMod.UNBUNDLER_BLOCK);
		this.registerItemBlock(RezolveMod.BUNDLE_BUILDER_BLOCK);
		this.registerItemBlock(RezolveMod.ETHERNET_CABLE_BLOCK);
		this.registerItemBlock(RezolveMod.REMOTE_SHELL_BLOCK);
		this.registerItemBlock(RezolveMod.DATABASE_SERVER_BLOCK);
		this.registerItemBlock(RezolveMod.SECURITY_SERVER_BLOCK);
		this.registerItemBlock(RezolveMod.DISK_BAY_BLOCK);
		this.registerItemBlock(RezolveMod.DISK_MANIPULATOR_BLOCK);
		this.registerItemBlock(RezolveMod.STORAGE_SHELL_BLOCK);
		this.registerItemBlock(RezolveMod.STORAGE_MONITOR_BLOCK);
		this.registerItemBlock(RezolveMod.MACHINE_FRAME_BLOCK);

		this.registerItemBlock(RezolveMod.METAL_ORE_BLOCK);
		this.registerItemBlock(RezolveMod.METAL_BLOCK);

	}

	protected void registerMachineParts() {
		MachinePart.register("activator");
		MachinePart.register("integrated_circuit");
		MachinePart.register("dac");
		MachinePart.register("adc");
		MachinePart.register("transcoder");
		MachinePart.register("display_panel");
	}

	protected void registerMetals() {
		Metal.COPPER.register();
		Metal.LEAD.register();
		Metal.TIN.register();
	}

	/**
	 * Register items!
	 */
	@Override
	protected void registerItems() {
		super.registerItems();

		this.registerItem(RezolveMod.BUNDLE_PATTERN_ITEM);
		this.registerItem(RezolveMod.BUNDLE_ITEM);
		this.registerItem(RezolveMod.DISK_ITEM);
		this.registerItem(RezolveMod.MACHINE_PART_ITEM);
		this.registerItem(RezolveMod.STORAGE_PART_ITEM);

		this.registerItem(RezolveMod.METAL_INGOT_ITEM);
		this.registerItem(RezolveMod.METAL_NUGGET_ITEM);
	}

	@Override
	protected void registerEntities() {
		super.registerEntities();

		this.registerEntity(
			RezolveMod.EntityRegistration
				.create(EntityDragon.class)
				.named("dragon")
				.withRenderer(RenderDragon::new)
				.withEgg(1447446, 894731)
				.updatesEvery(3)
				.trackedWithin(160)
				.sendsVelocityUpdates(true)
				.build()
		);

		EntityRegistry.addSpawn(
			EntityDragon.class,
			1,
			1, 1,
			EnumCreatureType.CREATURE,

			Biomes.EXTREME_HILLS,
			Biomes.DESERT,
			Biomes.EXTREME_HILLS_WITH_TREES,
			Biomes.HELL,
			Biomes.DEEP_OCEAN,
			Biomes.ICE_MOUNTAINS
		);
	}

	@Override
	protected void registerPackets() {
		super.registerPackets();

		GhostSlotUpdateMessageHandler.register();
		DragonUpdateMessageHandler.register();
		StorageViewMessageHandler.register();
		StorageViewRequestMessageHandler.register();
		StorageViewResponseMessageHandler.register();
		StorageViewStateMessageHandler.register();
		StorageViewRecipeRequestMessageHandler.register();
		StorageShellClearCrafterMessageHandler.register();
	}

	@Override
	public void registerItemRecipes() {
		super.registerItemRecipes();


		RecipeUtil.add(
			new ItemStack(MACHINE_PART_ITEM, 1, MACHINE_PART_ITEM.metadataFor("activator")),

			" B ",
			"RRR",
			"ISI",

			'B', "mc:stone_button",
			'R', "mc:redstone",
			'I', RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.TIN),
			'S', "mc:stone"
		);

		RecipeUtil.add(
			new ItemStack(MACHINE_PART_ITEM, 1, MACHINE_PART_ITEM.metadataFor("integrated_circuit")),

			"CSC",
			"CSC",
			"CRC",

			'C', RezolveMod.METAL_NUGGET_ITEM.getStackOf(Metal.COPPER),
			'S', "mc:slime_ball",
			'R', "mc:redstone"
		);

		RecipeUtil.add(
			new ItemStack(MACHINE_PART_ITEM, 1, MACHINE_PART_ITEM.metadataFor("dac")),

			" P ",
			"EiE",
			" R ",

			'P', "mc:heavy_weighted_pressure_plate",
			'E', "mc:ender_pearl",
			'i', "item_machine_part|integrated_circuit",
			'R', "mc:redstone"
		);

		RecipeUtil.add(
			new ItemStack(MACHINE_PART_ITEM, 1, MACHINE_PART_ITEM.metadataFor("adc")),

			" P ",
			"CiC",
			" R ",

			'P', "mc:heavy_weighted_pressure_plate",
			'C', "mc:chest",
			'i', "item_machine_part|integrated_circuit",
			'R', "mc:redstone"
		);

		RecipeUtil.add(
			new ItemStack(MACHINE_PART_ITEM, 1, MACHINE_PART_ITEM.metadataFor("transcoder")),

			" D ",
			"cic",
			" A ",

			'D', "item_machine_part|dac",
			'c', "block_ethernet_cable",
			'i', "item_machine_part|integrated_circuit",
			'A', "item_machine_part|adc"
		);

	}

	@Override
	protected void registerGenerators() {
		super.registerGenerators();

		new StonelessWorldType();

	}

	private IJeiRuntime jeiRuntime = null;

	public void setJeiRuntime(IJeiRuntime jeiRuntime) {
		this.jeiRuntime = jeiRuntime;
	}

	public IJeiRuntime getJeiRuntime() {
		return this.jeiRuntime;
	}
}
