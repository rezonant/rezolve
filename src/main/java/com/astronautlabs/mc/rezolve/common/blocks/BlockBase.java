package com.astronautlabs.mc.rezolve.common.blocks;

import com.astronautlabs.mc.rezolve.RezolveMod;

import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.worlds.Metal;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    protected RezolveMod mod;
    
    public void init(RezolveMod mod) {
    	this.mod = mod;
    }
    
    public void registerRecipes() {
    	
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        return List.of(new ItemStack(asItem()));
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        super.fillItemCategory(pTab, pItems);
    }

    public void applyTags(Consumer<RezolveRegistry.Tagger<Block>> configurer) {
        RezolveRegistry.registerForTagging(this, configurer);
    }

    private List<Consumer<RezolveRegistry.Tagger<Item>>> itemTagConfigurers = new ArrayList<>();

    public void applyItemTags(Consumer<RezolveRegistry.Tagger<Item>> configurer) {
        itemTagConfigurers.add(configurer);
    }

    /**
     * Do any initialization needed for the block item that represents this block.
     * This is used for tags initialization.
     * @param item
     */
    public void initializeItem(BlockItem item) {
        for (var configurer : itemTagConfigurers)
            RezolveRegistry.registerForTagging(item, configurer);
    }
}
