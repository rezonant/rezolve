package com.astronautlabs.mc.rezolve.core;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.common.IGuiProvider;
import com.astronautlabs.mc.rezolve.common.IOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ModGuiHandler implements IGuiHandler {

	public ModGuiHandler() {
		MinecraftForge.EVENT_BUS.register(this);
		mc = Minecraft.getMinecraft();
	}
	
    public static final int MOD_TILE_ENTITY_GUI = 0;

	private Minecraft mc; 
    private ArrayList<IGuiProvider> guis = new ArrayList<IGuiProvider>();
    private ArrayList<IOverlay> overlays = new ArrayList<IOverlay>();
    
    public void addOverlay(IOverlay gui)
    {
    	synchronized (this.overlays) {
    		this.overlays.add(gui);
    	}
    }
    
    public void removeOverlay(IOverlay gui) {
    	synchronized (this.overlays) {
    		this.overlays.remove(gui);
    	}
    }
    
    @SubscribeEvent
    public void onRenderGui(GuiScreenEvent.DrawScreenEvent.Post event)
    {
    	IOverlay[] overlaySet;
    	synchronized (this.overlays) {
        	overlaySet = this.overlays.toArray(new IOverlay[this.overlays.size()]);
    	}
    	
    	GlStateManager.disableDepth();
		for (IOverlay overlay : overlaySet) {
			overlay.draw();
		}
    	
    	GlStateManager.enableDepth();

    	MouseEvent mev = new MouseEvent();
    	
		for (IOverlay overlay : overlaySet) {
			overlay.onMouseEvent(mev);
		}
    }
    
    public int registerGui(IGuiProvider provider) {
    	int id = this.guis.size();
    	this.guis.add(provider);
    	return id;
    }
    
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    	
	    	if (ID >= this.guis.size())
	    		return null;
	    	
	    	return this.guis.get(ID).createServerGui(player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
	
	    	if (ID >= this.guis.size())
	    		return null;
	    	
	    	return this.guis.get(ID).createClientGui(player, world, x, y, z);
    }
}
