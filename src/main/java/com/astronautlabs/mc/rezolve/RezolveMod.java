package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.cable.EthernetCableBlock;
import com.astronautlabs.mc.rezolve.cities.CityBiome;
import com.astronautlabs.mc.rezolve.cities.CityGenerator;
import com.astronautlabs.mc.rezolve.cities.CityMapGen;
import com.astronautlabs.mc.rezolve.inventory.GhostSlotUpdateMessageHandler;
import com.astronautlabs.mc.rezolve.machines.bundleBuilder.BundleBuilderBlock;
import com.astronautlabs.mc.rezolve.machines.bundleBuilder.BundlePatternItem;
import com.astronautlabs.mc.rezolve.machines.bundler.BundlerBlock;
import com.astronautlabs.mc.rezolve.machines.databaseServer.DatabaseServerBlock;
import com.astronautlabs.mc.rezolve.machines.diskBay.DiskBayBlock;
import com.astronautlabs.mc.rezolve.machines.diskBay.DiskItem;
import com.astronautlabs.mc.rezolve.machines.diskManipulator.DiskManipulatorBlock;
import com.astronautlabs.mc.rezolve.machines.remoteShell.RemoteShellBlock;
import com.astronautlabs.mc.rezolve.machines.securityServer.SecurityServerBlock;
import com.astronautlabs.mc.rezolve.machines.unbundler.UnbundlerBlock;
import com.astronautlabs.mc.rezolve.mobs.dragon.DragonUpdateMessageHandler;
import com.astronautlabs.mc.rezolve.mobs.dragon.EntityDragon;
import com.astronautlabs.mc.rezolve.mobs.dragon.RenderDragon;
import com.astronautlabs.mc.rezolve.terrain.StonelessWorldType;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
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


	// Items
	public static final BundlePatternItem BUNDLE_PATTERN_ITEM = new BundlePatternItem();
	public static final DiskItem DISK_ITEM = new DiskItem();
	public static final BundleItem BUNDLE_ITEM = new BundleItem();

	@SidedProxy(clientSide = "com.astronautlabs.mc.rezolve.ClientProxy", serverSide = "com.astronautlabs.mc.rezolve.ServerProxy")
	public static CommonProxy proxy;

	public static final CityBiome CITY_BIOME = new CityBiome();
	public static final CityGenerator CITY_GENERATOR = new CityGenerator();

	private CityMapGen cityMapGen;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		System.out.println("Starting Rezolve @VERSION@...");
		super.preinit(event);

		this.cityMapGen = new CityMapGen();
		proxy.init(this);

		FMLInterModComms.sendMessage("Waila", "register", "com.astronautlabs.mc.rezolve.waila.WailaCompat.load");

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
	}

	@Override
	protected void registerGenerators() {
		super.registerGenerators();

		new StonelessWorldType();

	}
}
