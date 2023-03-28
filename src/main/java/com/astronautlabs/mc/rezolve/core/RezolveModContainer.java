//package com.astronautlabs.mc.rezolve.core;
//
//import java.util.Arrays;
//import java.util.List;
//
//import com.google.common.eventbus.EventBus;
//import com.google.common.eventbus.Subscribe;
//
//import net.minecraftforge.fml.common.DummyModContainer;
//import net.minecraftforge.fml.common.LoadController;
//import net.minecraftforge.fml.common.ModMetadata;
//import net.minecraftforge.fml.common.event.FMLConstructionEvent;
//import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
//
//public class RezolveModContainer extends DummyModContainer {
//	public RezolveModContainer() {
//		super(new ModMetadata());
//
//		ModMetadata meta = getMetadata();
//
//		meta.modId = "rezolvecore";
//		meta.name = "RezolveCore";
//		meta.version = "@VERSION@";
//		meta.credits = "By William Lahti (rezonant)";
//		meta.authorList = Arrays.asList(new String[] { "rezonant" });
//		meta.description = "RezolveCore provides core modifications needed for Rezolve. Specifically, modifies Player to allow the Remote Shell to override machine access restrictions related to the player's distance from the block.";
//		meta.url = "https://minecraft.curseforge.com/projects/rezolve";
//		meta.screenshots = new String[0];
//
//		// I think it's a bummer that if you declare a parent, then the information is no longer browseable in the
//		// mods list. So I'm not declaring it.
//		// meta.parent = "rezolve";
//		meta.logoFile = "assets/rezolve/logo.png";
//	}
//
//	@Override
//	public boolean registerBus(EventBus bus, LoadController controller) {
//		return true;
//	}
//
//	@Subscribe
//	public void modConstruction(FMLConstructionEvent evt) {
//
//	}
//
//	@Subscribe
//	public void preInit(FMLPreInitializationEvent evt) {
//
//	}
//
//	@Subscribe
//	public void init(FMLInitializationEvent evt) {
//
//	}
//
//	@Subscribe
//	public void postInit(FMLPostInitializationEvent evt) {
//
//	}
//}
