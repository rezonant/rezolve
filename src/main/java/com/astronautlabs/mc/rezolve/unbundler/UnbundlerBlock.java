package com.astronautlabs.mc.rezolve.unbundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.bundler.BundlerContainer;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity;
import com.astronautlabs.mc.rezolve.bundler.BundlerGuiContainer;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {

			RezolveMod.addRecipe(
				new ItemStack(this.itemBlock), 
				"BCB",
				"EME",
				"BcB", 
				
				'B', RezolveMod.blankBundlePatternItem,
				'C', "block|enderio:blockCapBank",
				'E', "item|enderio:itemMagnet",
				'M', "item|enderio:itemMachinePart|0",
				'c', "item|enderio:itemItemConduit"
			);
			
		} else {
			RezolveMod.addRecipe(
				new ItemStack(this.itemBlock), 
				"PcP",
				"CpC",
				"PHP", 
				
				'P', RezolveMod.blankBundlePatternItem,
				'c', Blocks.CRAFTING_TABLE,
				'C', Blocks.CHEST,
				'p', Blocks.PISTON,
				'H', Blocks.HOPPER
			);
		}
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
