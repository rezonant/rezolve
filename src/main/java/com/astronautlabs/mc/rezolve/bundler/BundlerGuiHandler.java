package com.astronautlabs.mc.rezolve.bundler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.common.IGuiProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class BundlerGuiHandler implements IGuiHandler {

    public static final int MOD_TILE_ENTITY_GUI = 0;

    private ArrayList<IGuiProvider> guis = new ArrayList<IGuiProvider>();
    
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
