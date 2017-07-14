package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileBlockBase extends BlockBase implements ITileEntityProvider {
	TileBlockBase(String registryName) {
		super(registryName);
        this.isBlockContainer = true;
	}
	
	@Override
	public abstract TileEntity createNewTileEntity(World worldIn, int meta);

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {

    	// Drop any items we have if necessary
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof IInventory) {
			InventoryHelper.dropInventoryItems(world, pos, (IInventory)te);
		}
	    
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventId, int eventParam) {
        super.eventReceived(state, worldIn, pos, eventId, eventParam);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity == null ? false : tileentity.receiveClientEvent(eventId, eventParam);
    }
}
