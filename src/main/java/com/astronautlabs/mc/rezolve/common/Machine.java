package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class Machine extends TileBlockBase implements IGuiProvider {
	public Machine(String registryName) {
		super(registryName);
	}
	
	@Override()
	public void init(RezolveMod mod) {
		super.init(mod);
		
		this.guiId = mod.getGuiHandler().registerGui(this);
	}
	
	private int guiId = -1;
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		if (!world.isRemote) {
	        player.openGui(this.mod, this.guiId, world, pos.getX(), pos.getY(), pos.getZ());
	    }
		
	    return true;
	}

	@Override
	public abstract Container createServerGui(EntityPlayer player, World world, int x, int y, int z);

	@Override
	public abstract GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z);
}
