package com.astronautlabs.mc.rezolve;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.astronautlabs.mc.rezolve.bundleBuilder.BlankBundlePatternItem;
import com.astronautlabs.mc.rezolve.bundleBuilder.BundleBuilderBlock;
import com.astronautlabs.mc.rezolve.bundleBuilder.BundlePatternItem;
import com.astronautlabs.mc.rezolve.bundler.BundlerBlock;
import com.astronautlabs.mc.rezolve.common.BlockBase;
import com.astronautlabs.mc.rezolve.common.GhostSlotUpdateMessageHandler;
import com.astronautlabs.mc.rezolve.common.ItemBase;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.unbundler.UnbundlerBlock;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = RezolveMod.MODID, version = RezolveMod.VERSION, name = "Rezolve", dependencies = "after:Waila;after:EnderIO")
public class RezolveMod {
	public RezolveMod() {
		_instance = this;
	}

	public static final String MODID = "rezolve";
	public static final String VERSION = "1.0";

	public static BundlerBlock bundlerBlock;
	public static UnbundlerBlock unbundlerBlock;
	public static BundleBuilderBlock bundleBuilderBlock;
	public static ItemBlock bundlerItem;
	public static BundlePatternItem bundlePatternItem;
	public static BlankBundlePatternItem blankBundlePatternItem;
	public static BundleItem bundleItem;
	public static ArrayList<BundleItem> coloredBundleItems = new ArrayList<BundleItem>();

	public static final Logger logger = LogManager.getLogger(RezolveMod.MODID);
	private static RezolveMod _instance = null;

	public static RezolveMod instance() {
		return _instance;
	}

	public String getColorName(int dye) {
		if (dye < 0 || dye >= dyeNames.length)
			return "";

		return dyeNames[dye];
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

		this.registerItemBlock(bundlerBlock = new BundlerBlock());
		this.registerItemBlock(unbundlerBlock = new UnbundlerBlock());
		this.registerItemBlock(bundleBuilderBlock = new BundleBuilderBlock());
	}

	/**
	 * Register items!
	 */
	private void registerItems() {
		this.log("Registering items...");

		this.registerItem(bundlePatternItem = new BundlePatternItem());
		this.registerItem(blankBundlePatternItem = new BlankBundlePatternItem());

		this.registerItem(bundleItem = new BundleItem());
		for (String color : dyes)
			this.registerColoredBundleItem(color);
	}

	public boolean isBundleItem(Item item) {
		if (bundleItem.getRegistryName().equals(item.getRegistryName()))
			return true;

		for (BundleItem coloredBundleItem : coloredBundleItems) {
			if (coloredBundleItem.getRegistryName().equals(item.getRegistryName()))
				return true;
		}

		return false;
	}

	public boolean isBundlePatternItem(Item item) {
		if (bundlePatternItem.getRegistryName().equals(item.getRegistryName()))
			return true;
		if (blankBundlePatternItem.getRegistryName().equals(item.getRegistryName()))
			return true;

		return false;
	}

	private static final String[] dyes = new String[] { "black", "red", "green", "brown", "blue", "purple", "cyan",
			"light_gray", "gray", "pink", "lime", "yellow", "light_blue", "magenta", "orange", "white" };

	private static final String[] dyeNames = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan",
			"Light Gray", "Gray", "Pink", "Lime", "Yellow", "Light Blue", "Magenta", "Orange", "White" };

	private void registerColoredBundleItem(String color) {
		BundleItem item = new BundleItem(color);
		coloredBundleItems.add(item);
		this.registerItem(item);
	}

	RezolveGuiHandler guiHandler;

	public RezolveGuiHandler getGuiHandler() {
		return this.guiHandler;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent event) {
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
