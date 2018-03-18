package com.astronautlabs.mc.rezolve.machines.remoteShell;

import com.astronautlabs.mc.rezolve.ModBase;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RemoteShellBlock extends Machine {

	public RemoteShellBlock() {
		super("block_remote_shell");
		
	}

	@Override
	public void registerRecipes() {
		ItemStack enderPearlBundle = RezolveMod.BUNDLE_ITEM.withContents(
			1,
			new ItemStack(Items.ENDER_PEARL, 16)
		);
		
		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
			RecipeUtil.add(
				new ItemStack(this.itemBlock), 
				"cCc",
				"EME",
				"cec", 
				
				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
				'C', Items.COMPARATOR,
				'E', enderPearlBundle,
				'M', Item.REGISTRY.getObject(new ResourceLocation("enderio:itemMachinePart")),
				'e', Blocks.ENDER_CHEST
			);
		} else {
			RecipeUtil.add(
				new ItemStack(this.itemBlock), 
				"cCc",
				"ERE",
				"tet", 
				
				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
				'C', Items.COMPARATOR,
				'E', enderPearlBundle,
				'R', Blocks.REDSTONE_BLOCK,
				't', Blocks.REDSTONE_TORCH,
				'e', Blocks.ENDER_CHEST
			);
		}
	}
	
	@Override
	public void init(ModBase mod) {
		super.init(mod);
		RemoteShellActivateMessageHandler.register();
		RemoteShellReturnMessageHandler.register();
		RemoteShellRenameMachineMessageHandler.register();
	}
	
	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return RemoteShellEntity.class;
	}
	
	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new RemoteShellContainer(player.inventory, (RemoteShellEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new RemoteShellGuiContainer(player.inventory, (RemoteShellEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}

}
