package com.astronautlabs.mc.rezolve;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.astronautlabs.mc.rezolve.bundles.BundleItem;
import com.astronautlabs.mc.rezolve.bundles.bundleBuilder.BundlePatternItem;
import com.astronautlabs.mc.rezolve.common.LevelPosition;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.common.util.ShiftedPlayer;
import com.astronautlabs.mc.rezolve.parts.MachineFrameBlock;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskBayBlock;
import com.astronautlabs.mc.rezolve.storage.machines.diskManipulator.DiskManipulatorBlock;
import com.astronautlabs.mc.rezolve.storage.machines.storageMonitor.StorageMonitorBlock;
import com.astronautlabs.mc.rezolve.storage.machines.storageShell.StorageShellBlock;
import com.astronautlabs.mc.rezolve.thunderbolt.cable.*;
import com.astronautlabs.mc.rezolve.thunderbolt.proxy.ProxyBlock;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.*;
import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.SecurityServerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.astronautlabs.mc.rezolve.bundles.bundleBuilder.BundleBuilder;
import com.astronautlabs.mc.rezolve.bundles.bundler.Bundler;
import com.astronautlabs.mc.rezolve.thunderbolt.databaseServer.DatabaseServer;
import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.SecurityServer;
import com.astronautlabs.mc.rezolve.bundles.unbundler.Unbundler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod(RezolveMod.ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RezolveMod {
	public static final String ID = "rezolve";
	public static final Logger LOGGER = LogManager.getLogger(RezolveMod.ID);

	public RezolveMod() {
		_instance = this;
		RezolveRegistry.register(
			Bundler.class,
			Unbundler.class,
			SecurityServer.class,
			DatabaseServer.class,
			BundleBuilder.class,
			BundleItem.class,
			BundlePatternItem.class,
			ThunderboltCable.class,
//			BlueThunderboltCable.class,
//			GreenThunderboltCable.class,
//			OrangeThunderboltCable.class,
			RemoteShellBlock.class,
			ProxyBlock.class,
			DiskBayBlock.class,
			DiskManipulatorBlock.class,
			StorageShellBlock.class,
			StorageMonitorBlock.class,
			MachineFrameBlock.class
		);
	}

	public static ResourceLocation loc(String name) {
		return new ResourceLocation(RezolveMod.ID, name);
	}
	public static void setPlayerOverridePosition(UUID playerID, LevelPosition pos) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.put(playerID.toString(), pos);
		}
	}
	
	public static void clearPlayerOverridePosition(UUID playerID) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.remove(playerID.toString());
		}
	}
	
	public static Map<String, LevelPosition> playerOverridePositions = new HashMap<>();
	
	/**
	 * Determine if the player is allowed to interact with the given UI container.
	 * This override is used whenever container.stillValid(player) is called in Vanilla.
	 * Its purpose is to enable the Remote Shell.
	 */
	public static boolean stillValid(AbstractContainerMenu container, Player player) {
		if (container.stillValid(player))
			return true;

		// Container is rejecting player, override if available
		
		synchronized (playerOverridePositions) {
			if (!playerOverridePositions.containsKey(player.getStringUUID()))
				return false;

			var overriddenPosition = playerOverridePositions.get(player.getStringUUID());
			return container.stillValid(new ShiftedPlayer(player, overriddenPosition));
		}
	}


	private static RezolveMod _instance = null;
	
	public static RezolveMod instance() {
		return _instance;
	}

	public static SecurityServerEntity getGoverningSecurityServer(Level level, BlockPos blockPos) {
		var networks = CableNetwork.getNetworksForEndpoint(level, blockPos);
		for (var network : networks) {
			if (network.getSecurityServer() != null)
				return network.getSecurityServer();
		}

		return null;
	}

	public static InteractionResult useSecurely(Block block, BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide)
			return block.use(state, level, blockPos, player, hand, hitResult);

		var securityServer = RezolveMod.getGoverningSecurityServer(level, blockPos);
		if (securityServer != null && !securityServer.canPlayerUse(player, blockPos)) {
			if (!level.isClientSide) {
				player.sendSystemMessage(Component.empty()
						.append(Component.translatable("rezolve.securityserver.access_denied"))
						.append(" (")
						.append(Component.translatable("rezolve.securityserver.owned_by"))
						.append(" ")
						.append(securityServer.getRootUser() != null ? securityServer.getRootUser() : "<no one>")
						.append(")")
				);
			}
			return InteractionResult.CONSUME;
		}

		return block.use(state, level, blockPos, player, hand, hitResult);
	}

	public String getColorName(int dye) {
		if (dye < 0 || dye >= DYE_NAMES.length)
			return "";

		return DYE_NAMES[dye];
	}

	@Deprecated
	public static boolean areStacksSame(ItemStack stackA, ItemStack stackB) {
		return ItemStack.isSame(stackA, stackB);
	}

	public boolean isDye(Item item) {
		return item instanceof DyeItem;
	}
	
	public static final String[] DYES = new String[] { "black", "red", "green", "brown", "blue", "purple", "cyan",
			"light_gray", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white" };

	public static final String[] DYE_NAMES = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan",
			"Light Gray", "Gray", "Pink", "Lime", "Yellow", "Light Blue", "Magenta", "Orange", "White" };

//	RezolveGuiHandler guiHandler;
//
//	public RezolveGuiHandler getGuiHandler() {
//		return this.guiHandler;
//	}

//	public static final CityBiome CITY_BIOME = new CityBiome();
//	public static final CityGenerator CITY_GENERATOR = new CityGenerator();

	@SubscribeEvent
	public static void register(RegisterEvent event) {
		boolean enabled = false;
		if (enabled) {
//			GameRegistry.register(CITY_BIOME);
//			GameRegistry.registerWorldGenerator(CITY_GENERATOR, Integer.MAX_VALUE);
//			BiomeProvider.allowedBiomes.clear();
//
//			int cityWeight = 3;
//
//			//cityWeight = 999999;
//			BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(CITY_BIOME, cityWeight));
//			BiomeManager.addBiome(BiomeType.WARM, new BiomeEntry(CITY_BIOME, cityWeight));
//			BiomeManager.addBiome(BiomeType.COOL, new BiomeEntry(CITY_BIOME, cityWeight));
//			BiomeManager.addBiome(BiomeType.ICY, new BiomeEntry(CITY_BIOME, cityWeight));
//			BiomeManager.addSpawnBiome(CITY_BIOME);
		}
	}

	@SubscribeEvent
	public static void interModEnqueue(InterModEnqueueEvent event) {
		LOGGER.info("Starting Rezolve...");

//		if (this.enableCities)
//			this.cityMapGen.registerCities();
//
//		if (this.enableTowns)
//			this.cityMapGen.registerTowns();

		InterModComms.sendTo(RezolveMod.ID, "waila", "register", () -> "com.astronautlabs.mc.rezolve.waila.WailaCompat.load");
	}

//	public static final CityGenerator CITY_GENERATOR = new CityGenerator();
//	private CityMapGen cityMapGen = new CityMapGen();
}
