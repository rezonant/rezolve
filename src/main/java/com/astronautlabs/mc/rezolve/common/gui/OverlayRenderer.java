//package com.astronautlabs.mc.rezolve;
//
//import java.util.ArrayList;
//
//import net.minecraft.client.Minecraft;
//import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
//import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//@Mod.EventBusSubscriber(modid = RezolveMod.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
//public class OverlayRenderer implements IGuiHandler {
//
//    OverlayRenderer() {
//        mc = Minecraft.getInstance();
//    }
//
//    public static final int MOD_TILE_ENTITY_GUI = 0;
//
//    private Minecraft mc;
//    private ArrayList<IGuiProvider> guis = new ArrayList<IGuiProvider>();
//    private ArrayList<IOverlay> overlays = new ArrayList<IOverlay>();
//
//    public void addOverlay(IOverlay gui)
//    {
//        synchronized (this.overlays) {
//            this.overlays.add(gui);
//        }
//    }
//
//    public void removeOverlay(IOverlay gui) {
//        synchronized (this.overlays) {
//            this.overlays.remove(gui);
//        }
//    }
//
//    @SubscribeEvent
//    public void registerOverlay(RegisterGuiOverlaysEvent event) {
//        event.registerAboveAll();
//    }
//
//    @SubscribeEvent
//    public void onRenderGui(CustomizeGuiOverlayEvent event)
//    {
//        IOverlay[] overlaySet;
//        synchronized (this.overlays) {
//            overlaySet = this.overlays.toArray(new IOverlay[this.overlays.size()]);
//        }
//
//        GlStateManager.disableDepth();
//        for (IOverlay overlay : overlaySet) {
//            overlay.draw();
//        }
//
//        GlStateManager.enableDepth();
//
//        MouseEvent mev = new MouseEvent();
//
//        for (IOverlay overlay : overlaySet) {
//            overlay.onMouseEvent(mev);
//        }
//    }
//
//    public int registerGui(IGuiProvider provider) {
//        int id = this.guis.size();
//        this.guis.add(provider);
//        return id;
//    }
//
//    @Override
//    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//
//        if (ID >= this.guis.size())
//            return null;
//
//        return this.guis.get(ID).createServerGui(player, world, x, y, z);
//    }
//
//    @Override
//    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//
//        if (ID >= this.guis.size())
//            return null;
//
//        return this.guis.get(ID).createClientGui(player, world, x, y, z);
//    }
//}