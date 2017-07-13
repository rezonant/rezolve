package com.astronautlabs.mc.rezolve.common;

import java.util.ArrayList;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
		
		ArrayList<ItemStack> subtypes = new ArrayList<ItemStack>();
		this.getSubItems(this, this.getCreativeTab(), subtypes);
		
		for (ItemStack subtype : subtypes) {
			String unlocalizedName = this.getUnlocalizedName(subtype);
			String modelName = unlocalizedName.replace("item.", "");
			
			System.out.println("Registering "+this.getRegistryName()+" metadata "+subtype.getMetadata()+" as model "+modelName);
			ModelLoader.setCustomModelResourceLocation(
				this, subtype.getMetadata(), 
				new ModelResourceLocation(modelName, "inventory")
			);
		}

		//Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
	    //	.register(this, 0, new ModelResourceLocation(this.getRegistryName().toString(), "inventory"));
	}
}
