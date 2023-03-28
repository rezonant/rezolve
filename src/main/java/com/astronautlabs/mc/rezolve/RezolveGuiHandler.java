//package com.astronautlabs.mc.rezolve;
//
//import java.util.ArrayList;
//
//import com.astronautlabs.mc.rezolve.common.IGuiProvider;
//import com.astronautlabs.mc.rezolve.common.IOverlay;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Gui;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.GlStateManager;
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.client.event.GuiScreenEvent;
//import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
//import net.minecraftforge.client.event.MouseEvent;
//import net.minecraftforge.client.event.RenderGameOverlayEvent;
//import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.network.IGuiHandler;
//
//public class RezolveGuiHandler implements IGuiHandler {
//
//	RezolveGuiHandler() {
//		MinecraftForge.EVENT_BUS.register(this);
//		mc = Minecraft.getInstance();
//	}
//
//    public static final int MOD_TILE_ENTITY_GUI = 0;
//
//	private Minecraft mc;
//    private ArrayList<IGuiProvider> guis = new ArrayList<IGuiProvider>();
//    private ArrayList<IOverlay> overlays = new ArrayList<IOverlay>();
//
//    public void addOverlay(IOverlay gui)
//    {
//    	synchronized (this.overlays) {
//    		this.overlays.add(gui);
//    	}
//    }
//
//    public void removeOverlay(IOverlay gui) {
//    	synchronized (this.overlays) {
//    		this.overlays.remove(gui);
//    	}
//    }
//
//    @SubscribeEvent
//    public void onRenderGui(GuiScreenEvent.DrawScreenEvent.Post event)
//    {
//    	IOverlay[] overlaySet;
//    	synchronized (this.overlays) {
//        	overlaySet = this.overlays.toArray(new IOverlay[this.overlays.size()]);
//    	}
//
//    	RenderSystem.disableDepthTest();
//		for (IOverlay overlay : overlaySet) {
//			overlay.draw();
//		}
//
//    	RenderSystem.enableDepthTest();
//
//    	MouseEvent mev = new MouseEvent();
//
//		for (IOverlay overlay : overlaySet) {
//			overlay.onMouseEvent(mev);
//		}
//    }
//
//    public int registerGui(IGuiProvider provider) {
//    	int id = this.guis.size();
//    	this.guis.add(provider);
//    	return id;
//    }
//
//    @Override
//    public Object getServerGuiElement(int ID, Player player, Level world, int x, int y, int z) {
//
//	    	if (ID >= this.guis.size())
//	    		return null;
//
//	    	return this.guis.get(ID).createServerGui(player, world, x, y, z);
//    }
//
//    @Override
//    public Object getClientGuiElement(int ID, Player player, Level world, int x, int y, int z) {
//
//	    	if (ID >= this.guis.size())
//	    		return null;
//
//	    	return this.guis.get(ID).createClientGui(player, world, x, y, z);
//    }
//}
