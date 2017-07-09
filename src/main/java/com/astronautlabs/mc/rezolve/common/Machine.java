package com.astronautlabs.mc.rezolve.common;

import java.lang.reflect.Constructor;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class Machine extends TileBlockBase implements IGuiProvider {
	public Machine(String registryName) {
		super(registryName);
		RezolveMod.instance().registerTileEntity(this.getTileEntityClass());
	}
	
	@Override()
	public void init(RezolveMod mod) {
		super.init(mod);
		
		this.guiId = mod.getGuiHandler().registerGui(this);
	}
	
	private int guiId = -1;

	public abstract Class<? extends TileEntityBase> getTileEntityClass();
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (stack.hasDisplayName()) {
	        TileEntity entity = worldIn.getTileEntity(pos);
	        if (entity != null && entity instanceof TileEntityBase)
	        		((TileEntityBase)entity).setCustomName(stack.getDisplayName());
	    }
		
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		if (!world.isRemote) {
	        player.openGui(this.mod, this.guiId, world, pos.getX(), pos.getY(), pos.getZ());
	    }
		
	    return true;
	}

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
	public abstract Container createServerGui(EntityPlayer player, World world, int x, int y, int z);

	@Override
	public abstract GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z);
}
