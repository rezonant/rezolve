package com.astronautlabs.mc.rezolve.bundles.machines.unbundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
		RecipeUtil.add(
			new ItemStack(this.itemBlock),
			"PcP",
			"CMC",
			"PHP",

			'P', "item_bundle_pattern|blank",
			'c', "mc:crafting_table",
			'C', "mc:chest",
			'M', "block_machine_frame",
			'H', "mc:hopper"
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
