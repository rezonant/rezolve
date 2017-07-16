package com.astronautlabs.mc.rezolve.core;

import java.util.List;

import javax.management.modelmbean.ModelMBeanConstructorInfo;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import scala.actors.threadpool.Arrays;

public class RezolveModContainer extends DummyModContainer {
	public RezolveModContainer() {
		super(new ModMetadata());
		
		ModMetadata meta = getMetadata();
		
		meta.modId = "rezolvecore";
		meta.name = "Rezolve (Core)";
		meta.version = "@VERSION@";
		meta.credits = "Written by William Lahti (rezonant)";
		meta.authorList = Arrays.asList(new String[] { "William Lahti" });
		meta.description = "Core modifications needed for Rezolve";
		meta.url = "https://github.com/astronautlabs/rezolve";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		return true;
	}
	
	@Subscribe
	public void modConstruction(FMLConstructionEvent evt) {
		
	}
	
	@Subscribe
	public void preInit(FMLPreInitializationEvent evt) {
		
	}
	
	@Subscribe
	public void init(FMLInitializationEvent evt) {
		
	}
	
	@Subscribe
	public void postInit(FMLPostInitializationEvent evt) {
		
	}
}
