package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.Machine;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BundlerBlock extends Machine {
	public BundlerBlock() {
		super("block_bundler");
	}
	
	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		
		BundlerEntity.register();
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
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (stack.hasDisplayName()) {
	        ((BundlerEntity) worldIn.getTileEntity(pos)).setCustomName(stack.getDisplayName());
	    }
		
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new BundlerGuiContainer(player.inventory, (BundlerEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}
	
	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new BundlerContainer(player.inventory, (BundlerEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new BundlerEntity();
	}
	
}
