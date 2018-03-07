package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.*;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

		if (ItemUtil.registered("enderio:itemAlloy")) {
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
}
