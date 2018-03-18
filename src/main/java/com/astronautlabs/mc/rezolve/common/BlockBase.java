package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.ModBase;
import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;

public class BlockBase extends Block {
	public BlockBase(String registryName, Material material, float hardness, float resistance) {
		super(material);

    	this.setRegistryName(registryName);
    	this.setUnlocalizedName(this.getRegistryName().toString());

    	System.out.println("Creating block " + this.getRegistryName().toString());
    	
		this.setHardness(hardness);
		this.setResistance(resistance);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	public ItemBlock itemBlock;
	
    public BlockBase(String unlocalizedName, float hardness, float resistance) {
        this(unlocalizedName, Material.ROCK, hardness, resistance);
    }

    public BlockBase(String unlocalizedName) {
        this(unlocalizedName, 2.0f, 10.0f);
    }

    protected ModBase mod;

    public void init(ModBase mod) {
    	this.mod = mod;
    }
    
    public void registerRecipes() {
    	
    }
    
    public void registerRenderer() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
