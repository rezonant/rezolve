package com.astronautlabs.mc.rezolve;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.astronautlabs.mc.rezolve.bundles.BundleItem;
import com.astronautlabs.mc.rezolve.bundles.bundleBuilder.BundlePatternItem;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.common.util.ShiftedPlayer;
import com.astronautlabs.mc.rezolve.thunderbolt.cable.*;
import com.astronautlabs.mc.rezolve.thunderbolt.proxy.ProxyBlock;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.*;
import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.SecurityServerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
			ProxyBlock.class
		);
	}

	public static void setPlayerOverridePosition(UUID playerID, CableNetwork.LevelPosition pos) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.put(playerID.toString(), pos);
		}
	}
	
	public static void clearPlayerOverridePosition(UUID playerID) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.remove(playerID.toString());
		}
	}
	
	public static Map<String, CableNetwork.LevelPosition> playerOverridePositions = new HashMap<>();
	
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

	/**
	 * Offers much better error messages compared to the standard recipe registration method 
	 * while also being a bit less verbose. If a string is passed instead of an Item/Block, it will 
	 * be looked up using the appropriate registry (Item.REGISTRY for a prefix of "item|" and Block.REGISTRY
	 * for a prefix of "block|"). If a null value is encountered, an explanatory exception will be thrown to crash 
	 * the game. Also helpful during development if you aren't sure what the IDs are for a mod's item or block.
	 * 
	 * I suspect this will be great for getting to the root of crash reports from users as well.
	 * 
	 * @param output
	 * @param params
	 */
	public static void addRecipe(ItemStack output, Object... params) {
//		Character lastChar = null;
//		Object[] resolvedParams = new Object[params.length];
//		int index = 0;
//
//		for (Object param : params) {
//			int thisIndex = index;
//			resolvedParams[index++] = param;
//
//			if (param instanceof String && lastChar == null)
//				continue;
//
//			if (param instanceof Character) {
//				lastChar = (Character)param;
//				continue;
//			}
//
//			if (param == null) {
//				if (lastChar != null) {
//					throw new RuntimeException(
//						"The recipe ingredient labelled '"+lastChar+"' used in '"+output.getItem().toString()+"' could not be loaded, "
//						+ "this indicates that a mod has removed/renamed an item or block "
//						+ "and Rezolve has not been updated to match yet :-(. Please file a bug "
//						+ "and include the versions of Rezolve and the other mod."
//					);
//				}
//			}
//
//			if (param instanceof String) {
//				Object resolvedParam = null;
//				String identifier = (String)param;
//				ResourceLocation resloc = null;
//				String[] parts = identifier.split("\\|");
//
//				if ("item".equals(parts[0]))
//					resolvedParam = ForgeRegistries.ITEMS.getValue(resloc = new ResourceLocation(parts[1]));
//				else if ("block".equals(parts[0]))
//					resolvedParam = ForgeRegistries.BLOCKS.getValue(resloc = new ResourceLocation(parts[1]));
//				else
//					throw new RuntimeException("Invalid recipe identifier: "+identifier);
//
//				if (parts.length > 2) {
//					// Metadata
//
//					if (resolvedParam instanceof Item)
//						resolvedParam = new ItemStack((Item)resolvedParam, 1, Integer.parseInt(parts[2]));
//					else if (resolvedParam instanceof Block)
//						resolvedParam = new ItemStack((Block)resolvedParam, 1, Integer.parseInt(parts[2]));
//					else
//						throw new RuntimeException("Resolved parameter is not a block or item, cannot create an ItemStack from it.");
//				}
//
//				if (resolvedParam == null) {
//
//					System.out.println("Cannot find "+identifier);
//					System.out.println("Registered items in mod "+resloc.getNamespace()+" are:");
//					for (ResourceLocation loc : ForgeRegistries.ITEMS.getKeys()) {
//						if (!loc.getNamespace().equals(resloc.getNamespace()))
//							continue;
//
//						System.out.println(" - "+loc.getPath());
//					}
//
//					System.out.println("Registered blocks in mod "+resloc.getNamespace()+" are:");
//					for (ResourceLocation loc : ForgeRegistries.BLOCKS.getKeys()) {
//						if (!loc.getNamespace().equals(resloc.getNamespace()))
//							continue;
//
//						System.out.println(" - "+loc.getPath());
//					}
//
//					throw new RuntimeException(
//						"The recipe ingredient '"+identifier+"' used in '"+output.getItem().toString()+"' could not be loaded, "
//						+ "this indicates that a mod has removed/renamed an item or block "
//						+ "and Rezolve has not been updated to match yet :-(. Please file a bug "
//						+ "and include the versions of Rezolve and the other mod."
//					);
//				}
//
//				resolvedParams[thisIndex] = resolvedParam;
//			}
//		}
//
//		GameRegistry.addRecipe(output, resolvedParams);
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
		InterModComms.sendTo(RezolveMod.ID, "waila", "register", () -> "com.astronautlabs.mc.rezolve.waila.WailaCompat.load");
	}

}
