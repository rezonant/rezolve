package com.astronautlabs.mc.rezolve.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ItemBase extends Item {
	public ItemBase(String registryName) {
		super();
		
		this.setRegistryName(registryName);
		this.setUnlocalizedName(this.getRegistryName().toString());
		this.setCreativeTab(CreativeTabs.MISC);
	}
	
	public void registerRecipes() {
		
	}
	
	public void registerRenderer() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));

		//Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
	    //	.register(this, 0, new ModelResourceLocation(this.getRegistryName().toString(), "inventory"));
	}
}
