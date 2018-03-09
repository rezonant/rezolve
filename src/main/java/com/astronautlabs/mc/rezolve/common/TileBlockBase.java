package com.astronautlabs.mc.rezolve.common;

import java.lang.reflect.Constructor;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileBlockBase extends BlockBase implements ITileEntityProvider {
	public TileBlockBase(String registryName) {
		super(registryName);
        this.isBlockContainer = true;
	}
	
	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		RezolveMod.instance().registerTileEntity(this.getTileEntityClass());
	}

	public abstract Class<? extends TileEntityBase> getTileEntityClass();

	/**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     * 
     * PERFORMANCE NOTE: Default implementation relies on reflection, if performance is an issue (due to lots of 
     * tile entities being created at the same time) then this should be overridden to use 
     * a standard invocation.
     */
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		Class<? extends TileEntityBase> klass = this.getTileEntityClass();
		
		if (klass == null)
			return null;
		
		Constructor<? extends TileEntityBase> ctor;
		TileEntityBase instance;
		
		try {
			ctor = klass.getConstructor();
			instance = ctor.newInstance();
		} catch (Exception e) {
			System.err.println("Cannot construct tile entity "+klass.getCanonicalName()+": "+e.getMessage());
			System.err.println(e.toString());
			return null;
		}
		
		return instance;
	}

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
