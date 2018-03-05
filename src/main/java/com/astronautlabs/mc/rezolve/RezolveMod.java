package com.astronautlabs.mc.rezolve;

import java.lang.reflect.Constructor;
import java.util.*;

import com.astronautlabs.mc.rezolve.cities.CityNode;
import com.astronautlabs.mc.rezolve.mapgen.MapGenCave;
import com.astronautlabs.mc.rezolve.mapgen.MapGenRavine;
import com.astronautlabs.mc.rezolve.mapgen.MapGenScatteredFeature;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.astronautlabs.mc.rezolve.bundleBuilder.BundleBuilderBlock;
import com.astronautlabs.mc.rezolve.bundleBuilder.BundlePatternItem;
import com.astronautlabs.mc.rezolve.bundler.BundlerBlock;
import com.astronautlabs.mc.rezolve.cities.CityBiome;
import com.astronautlabs.mc.rezolve.cities.CityGenerator;
import com.astronautlabs.mc.rezolve.common.BlockBase;
import com.astronautlabs.mc.rezolve.common.GhostSlotUpdateMessageHandler;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.databaseServer.DatabaseServerBlock;
import com.astronautlabs.mc.rezolve.remoteShell.EthernetCableBlock;
import com.astronautlabs.mc.rezolve.remoteShell.RemoteShellBlock;
import com.astronautlabs.mc.rezolve.securityServer.SecurityServerBlock;
import com.astronautlabs.mc.rezolve.unbundler.UnbundlerBlock;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import scala.tools.nsc.backend.icode.TypeKinds;

@Mod(modid = RezolveMod.MODID, version = RezolveMod.VERSION, name = "Rezolve", dependencies = "after:Waila;after:EnderIO")
public class RezolveMod {
	public RezolveMod() {
		_instance = this;
	}

	public static void setPlayerOverridePosition(UUID playerID, BlockPos pos) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.put(playerID.toString(), pos);
		}
	}
	
	public static void clearPlayerOverridePosition(UUID playerID) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.remove(playerID.toString());
		}
	}
	
	public static Map<String, BlockPos> playerOverridePositions = new HashMap<String, BlockPos>();
	
	/**
	 * Determine if the player is allowed to interact with the given UI container.
	 * This overrides the EntityPlayer.onUpdate() check for container.canInteractWith(player).
	 * 
	 * @param container
	 * @param player
	 * @return
	 */
	public static boolean canInteractWith(Object containerObj, Object playerObj) {
		
		Container container = (Container)containerObj;
		EntityPlayer player = (EntityPlayer)playerObj;
		
		// Security check
		
		
		if (container.canInteractWith(player))
			return true;
		
		// Container is rejecting player, override if available
		
		synchronized (playerOverridePositions) {
			if (!playerOverridePositions.containsKey(player.getUniqueID().toString()))
				return false;

			BlockPos overriddenPosition = playerOverridePositions.get(player.getUniqueID().toString());
			boolean result = container.canInteractWith(new ShiftedPlayer(player, overriddenPosition));
			return result;
		}
	}
	
	public static final String MODID = "rezolve";
	public static final String VERSION = "1.0";

	// Blocks
	public static final BundlerBlock BUNDLER_BLOCK = new BundlerBlock();
	public static final UnbundlerBlock UNBUNDLER_BLOCK = new UnbundlerBlock();
	public static final BundleBuilderBlock BUNDLE_BUILDER_BLOCK = new BundleBuilderBlock();
	public static final EthernetCableBlock ETHERNET_CABLE_BLOCK = new EthernetCableBlock();
	public static final RemoteShellBlock REMOTE_SHELL_BLOCK = new RemoteShellBlock();
	public static final DatabaseServerBlock DATABASE_SERVER_BLOCK = new DatabaseServerBlock();
	public static final SecurityServerBlock SECURITY_SERVER_BLOCK = new SecurityServerBlock();

	// Items
	public static final BundlePatternItem BUNDLE_PATTERN_ITEM = new BundlePatternItem();
	public static final BundleItem BUNDLE_ITEM = new BundleItem();

	public static final Logger logger = LogManager.getLogger(RezolveMod.MODID);
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
		
		Character lastChar = null;
		Object[] resolvedParams = new Object[params.length];
		int index = 0;
		
		for (Object param : params) {
			int thisIndex = index;
			resolvedParams[index++] = param;
			
			if (param instanceof String && lastChar == null)
				continue;
			
			if (param instanceof Character) {
				lastChar = (Character)param;
				continue;
			}
			
			if (param == null) {
				if (lastChar != null) {
					throw new RuntimeException(
						"The recipe ingredient labelled '"+lastChar+"' used in '"+output.getItem().getRegistryName()+"' could not be loaded, "
						+ "this indicates that a mod has removed/renamed an item or block " 
						+ "and Rezolve has not been updated to match yet :-(. Please file a bug " 
						+ "and include the versions of Rezolve and the other mod."
					);
				}
			}
			
			if (param instanceof String) {
				Object resolvedParam = null;
				String identifier = (String)param;
				ResourceLocation resloc = null;
				String[] parts = identifier.split("\\|");
				
				if ("item".equals(parts[0]))
					resolvedParam = Item.REGISTRY.getObject(resloc = new ResourceLocation(parts[1]));
				else if ("block".equals(parts[0]))
					resolvedParam = Block.REGISTRY.getObject(resloc = new ResourceLocation(parts[1]));
				else 
					throw new RuntimeException("Invalid recipe identifier: "+identifier);
				
				if (parts.length > 2) {
					// Metadata 
					
					if (resolvedParam instanceof Item)
						resolvedParam = new ItemStack((Item)resolvedParam, 1, Integer.parseInt(parts[2]));
					else if (resolvedParam instanceof Block)
						resolvedParam = new ItemStack((Block)resolvedParam, 1, Integer.parseInt(parts[2]));
					else 
						throw new RuntimeException("Resolved parameter is not a block or item, cannot create an ItemStack from it.");
				}
				
				if (resolvedParam == null) {
					
					System.out.println("Cannot find "+identifier);
					System.out.println("Registered items in mod "+resloc.getResourceDomain()+" are:");
					for (ResourceLocation loc : Item.REGISTRY.getKeys()) {
						if (!loc.getResourceDomain().equals(resloc.getResourceDomain()))
							continue;
						
						System.out.println(" - "+loc.getResourcePath());
					}
					
					System.out.println("Registered blocks in mod "+resloc.getResourceDomain()+" are:");
					for (ResourceLocation loc : Block.REGISTRY.getKeys()) {
						if (!loc.getResourceDomain().equals(resloc.getResourceDomain()))
							continue;
						
						System.out.println(" - "+loc.getResourcePath());
					}
					
					throw new RuntimeException(
						"The recipe ingredient '"+identifier+"' used in '"+output.getItem().getRegistryName()+"' could not be loaded, "
						+ "this indicates that a mod has removed/renamed an item or block " 
						+ "and Rezolve has not been updated to match yet :-(. Please file a bug " 
						+ "and include the versions of Rezolve and the other mod."
					);
				}
				
				resolvedParams[thisIndex] = resolvedParam;
			}
		}
		
		GameRegistry.addRecipe(output, resolvedParams);
	}
	
	public String getColorName(int dye) {
		if (dye < 0 || dye >= DYE_NAMES.length)
			return "";

		return DYE_NAMES[dye];
	}

	public static boolean areStacksSame(ItemStack stackA, ItemStack stackB) {
		if (stackA == stackB)
			return true;
		
		if (stackA == null || stackB == null)
			return false;
		
		return (stackA.isItemEqual(stackB) && ItemStack.areItemStackTagsEqual(stackA, stackB));
	}

	@SidedProxy(clientSide = "com.astronautlabs.mc.rezolve.ClientProxy", serverSide = "com.astronautlabs.mc.rezolve.ServerProxy")
	public static CommonProxy proxy;

	private ArrayList<BlockBase> registeredBlocks = new ArrayList<BlockBase>();

	private void registerBlock(BlockBase block) {
		GameRegistry.register(block);
		this.registeredBlocks.add(block);

		block.init(this);
	}

	public void registerBlockRecipes() {
		this.log("Registering block recipes...");
		for (BlockBase block : this.registeredBlocks) {
			this.log("Registering recipes for: " + block.getRegistryName());
			block.registerRecipes();
		}
	}

	public void registerItemRecipes() {
		this.log("Registering item recipes...");
		for (ItemBase item : this.registeredItems) {
			this.log("Registering recipes for: " + item.getRegistryName());
			item.registerRecipes();
		}
	}

	public void registerBlockRenderers() {
		this.log("Registering block renderers...");
		for (BlockBase block : this.registeredBlocks) {
			this.log("Registering renderer for: " + block.getRegistryName());
			block.registerRenderer();
		}
	}

	public void registerItemRenderers() {
		this.log("Registering item renderers...");
		for (ItemBase item : this.registeredItems) {
			this.log("Registering renderer for: " + item.getRegistryName());
			item.registerRenderer();
		}
	}

	private ArrayList<ItemBase> registeredItems = new ArrayList<ItemBase>();

	public void registerItem(ItemBase item) {
		this.log("Registering item " + item.getRegistryName().toString());
		this.registeredItems.add(item);
		GameRegistry.register(item);
	}

	public void registerItemBlock(BlockBase block) {
		this.registerBlock(block);

		ItemBlock item = new ItemBlock(block);
		item.setRegistryName(block.getRegistryName());
		GameRegistry.register(item);

		block.itemBlock = item;
	}
	
	public void registerTileEntity(Class<? extends TileEntityBase> entityClass) {
		try {
			Constructor<? extends TileEntityBase> x = entityClass.getConstructor();
			
			TileEntityBase instance = x.newInstance();
			String registryName = instance.getRegistryName();
			
			GameRegistry.registerTileEntity(entityClass, registryName);
			
		} catch (Exception e) {
			 System.err.println("Cannot register tile entity class "+entityClass.getCanonicalName()+": Caught exception");
			 System.err.println(e.toString());
			 return;
		}
	}

	public boolean isDye(Item item) {
		return "minecraft:dye".equals(item.getRegistryName().toString());
	}

	/**
	 * Register blocks!
	 */
	private void registerBlocks() {
		this.log("Registering blocks...");

		this.registerItemBlock(BUNDLER_BLOCK);
		this.registerItemBlock(UNBUNDLER_BLOCK);
		this.registerItemBlock(BUNDLE_BUILDER_BLOCK);
		this.registerItemBlock(ETHERNET_CABLE_BLOCK);
		this.registerItemBlock(REMOTE_SHELL_BLOCK);
		this.registerItemBlock(DATABASE_SERVER_BLOCK);
		this.registerItemBlock(SECURITY_SERVER_BLOCK);
	}

	@SubscribeEvent
	public void initMapGen(InitMapGenEvent ev) {
		if (ev.getType() == InitMapGenEvent.EventType.RAVINE) {
			ev.setNewGen(new MapGenRavine());
		} else if (ev.getType() == InitMapGenEvent.EventType.SCATTERED_FEATURE) {
			ev.setNewGen(new MapGenScatteredFeature());
		} else if (ev.getType() == InitMapGenEvent.EventType.VILLAGE) {
			ev.setNewGen(new MapGenVillage());
		} else if (ev.getType() == InitMapGenEvent.EventType.CAVE) {
			ev.setNewGen(new MapGenCave());
		}
	}

	@SubscribeEvent
	public void populateChunk(PopulateChunkEvent.Populate ev) {

		List<PopulateChunkEvent.Populate.EventType> typesToBlock = Arrays.asList(
				PopulateChunkEvent.Populate.EventType.ANIMALS,
				PopulateChunkEvent.Populate.EventType.LAKE,
				PopulateChunkEvent.Populate.EventType.LAVA
				);

		if (typesToBlock.indexOf(ev.getType()) >= 0) {

			ChunkPos chunk = new ChunkPos(ev.getChunkX(), ev.getChunkZ());
			if (CityBiome.isCity(ev.getWorld(), chunk))
				ev.setResult(Event.Result.DENY);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void handleOverlay(RenderGameOverlayEvent.Text ev) {
		if (ev.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
				return;
			}

			World world = Minecraft.getMinecraft().theWorld;
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			ChunkPos chunkPos = new ChunkPos(new BlockPos((int)player.posX, (int)player.posY, (int)player.posZ));

			if (!CityBiome.isCity(world, chunkPos))
				return;

			ev.getLeft().clear();
			ev.getRight().clear();


			CityNode cityNode = CityNode.cityBlockFor(world, (int)player.posX, (int)player.posZ);

			ev.getLeft().add(String.format("City (%sc, %sc)", cityNode.getOriginX(), cityNode.getOriginZ()));
			cityNode.trace(ev.getLeft());

			// current node

			CityNode node = CityNode.nodeFor(world, (int)player.posX, (int)player.posY, (int)player.posZ);

			ev.getRight().add(String.format(
				"%s [%d, %d, %d] (%d, %d, %d)",
				node.getFeature().toString(),
				node.getSpanX(),
				node.getSpanY(),
				node.getSpanZ(),
				node.getOriginX(),
				node.getOriginY(),
				node.getOriginZ()
			));

			ev.getRight().add(String.format(
					"connects[north:%s, south:%s, east:%s, west:%s] - ",
					node.connectsNorth(world, chunkPos),
					node.connectsSouth(world, chunkPos),
					node.connectsEast(world, chunkPos),
					node.connectsWest(world, chunkPos)
			));

			ev.getRight().add("metadata - ");
			HashMap<String,Integer> params = node.getParameters();
			for (String key : params.keySet()) {
				int value = params.get(key);

				ev.getRight().add(value+" :"+key+" *    ");
			}

		}
	}

	/**
	 * Register items!
	 */
	private void registerItems() {
		this.log("Registering items...");

		this.registerItem(BUNDLE_PATTERN_ITEM);
		this.registerItem(BUNDLE_ITEM);
	}
	
	public static final String[] DYES = new String[] { "black", "red", "green", "brown", "blue", "purple", "cyan",
			"light_gray", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white" };

	public static final String[] DYE_NAMES = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan",
			"Light Gray", "Gray", "Pink", "Lime", "Yellow", "Light Blue", "Magenta", "Orange", "White" };

	RezolveGuiHandler guiHandler;

	public RezolveGuiHandler getGuiHandler() {
		return this.guiHandler;
	}

	public static final CityBiome CITY_BIOME = new CityBiome();
	public static final CityGenerator CITY_GENERATOR = new CityGenerator();

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.TERRAIN_GEN_BUS.register(this);

		System.out.println("Starting Rezolve @VERSION@...");

		boolean enabled = true;
		if (enabled) {

			GameRegistry.register(CITY_BIOME);
			GameRegistry.registerWorldGenerator(CITY_GENERATOR, Integer.MAX_VALUE);
			
			BiomeProvider.allowedBiomes.clear();
			
			int cityWeight = 4;
			
			//cityWeight = 999999;
			BiomeManager.addBiome(BiomeType.DESERT, new BiomeEntry(CITY_BIOME, cityWeight));
			BiomeManager.addBiome(BiomeType.WARM, new BiomeEntry(CITY_BIOME, cityWeight));
			BiomeManager.addBiome(BiomeType.COOL, new BiomeEntry(CITY_BIOME, cityWeight));
			BiomeManager.addBiome(BiomeType.ICY, new BiomeEntry(CITY_BIOME, cityWeight));
			BiomeManager.addSpawnBiome(CITY_BIOME);
		}
		
		this.guiHandler = new RezolveGuiHandler();
		this.registerBlocks();
		this.registerItems();

		this.registerBlockRecipes();
		this.registerItemRecipes();

		GhostSlotUpdateMessageHandler.register();
		
		proxy.init(this);

		FMLInterModComms.sendMessage("Waila", "register", "com.astronautlabs.mc.rezolve.waila.WailaCompat.load");

	}

	public void log(String message) {
		System.out.println(message);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		this.log("Initialized!");
	}
}
