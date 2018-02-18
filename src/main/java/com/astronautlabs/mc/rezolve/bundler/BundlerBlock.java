package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
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

public class BundlerBlock extends Machine {
	public BundlerBlock() {
		super("block_bundler");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return BundlerEntity.class;
	}
	
	@Override
	public void registerRecipes() {

		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
			RezolveMod.addRecipe(
				new ItemStack(this.itemBlock), 
				"VSV",
				"CMC",
				"VFV", 
				
				'V', "item|enderio:itemAlloy|2",
				'S', "block|minecraft:sticky_piston",
				'C', "block|minecraft:chest",
				'M', "item|enderio:itemMachinePart|0",
				'F', "item|enderio:itemBasicFilterUpgrade"
			);
			
		} else {
			RezolveMod.addRecipe(
				new ItemStack(this.itemBlock), 
				"IMI",
				"CSC",
				"IHI", 
				
				'I', "item|minecraft:iron_block",
				'M', "item|minecraft:minecart",
				'C', "item|minecraft:chest",
				'S', "item|minecraft:sticky_piston",
				'H', "item|minecraft:hopper"
			);
		}
	}
	
	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new BundlerGuiContainer(player.inventory, (BundlerEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}
	
	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new BundlerContainer(player.inventory, (BundlerEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}
	
}
