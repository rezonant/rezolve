package com.astronautlabs.mc.rezolve.unbundler;

import com.astronautlabs.mc.rezolve.bundler.BundlerContainer;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity;
import com.astronautlabs.mc.rezolve.bundler.BundlerGuiContainer;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class UnbundlerBlock extends Machine {

	public UnbundlerBlock() {
		super("block_unbundler");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return UnbundlerEntity.class;
	}
	
	@Override
	public void registerRecipes() {
		
		GameRegistry.addRecipe(new ItemStack(this.itemBlock), 
			"IMI",
			"CSC",
			"IHI", 
			
			'I', Item.REGISTRY.getObject(new ResourceLocation("minecraft:iron_block")),
			'M', Item.REGISTRY.getObject(new ResourceLocation("minecraft:minecart")),
			'C', Item.REGISTRY.getObject(new ResourceLocation("minecraft:chest")),
			'S', Item.REGISTRY.getObject(new ResourceLocation("minecraft:sticky_piston")),
			'H', Item.REGISTRY.getObject(new ResourceLocation("minecraft:hopper"))
		);
	}
	
	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new UnbundlerGuiContainer(player.inventory, (UnbundlerEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}
	
	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new UnbundlerContainer(player.inventory, (UnbundlerEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}
}
