package com.astronautlabs.mc.rezolve.common.blocks;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;

public class BlockBase extends Block {
	public BlockBase(BlockBehaviour.Properties properties) {
		super(properties);
	}

    public void register(DeferredRegister<Block> registry) {
        registry.register(this.getRegistryName(), () -> this);
    }

    protected String registryName;

    protected void setRegistryName(String name) {
        this.registryName = name;
    }

    public String getRegistryName() {
        return this.registryName;
    }

	public BlockItem itemBlock;
    protected RezolveMod mod;
    
    public void init(RezolveMod mod) {
    	this.mod = mod;
    }
    
    public void registerRecipes() {
    	
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        super.fillItemCategory(pTab, pItems);
    }

    //    public void registerRenderer() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }
}
