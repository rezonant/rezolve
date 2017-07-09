package com.astronautlabs.mc.rezolve.waila;

import java.util.List;

import com.astronautlabs.mc.rezolve.common.BlockBase;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WailaCompat implements IWailaDataProvider {
	public static final WailaCompat INSTANCE = new WailaCompat(); 
	
	public static void load(IWailaRegistrar registrar) {
		System.out.println("Bundler: Waila Registration....");
		//registrar.registerHeadProvider(INSTANCE, BlockBase.class);
		registrar.registerBodyProvider(INSTANCE, BlockBase.class);
		registrar.registerBodyProvider(INSTANCE, BlockBase.class);
		//registrar.registerNBTProvider(INSTANCE, TileEntityBase.class);
	}
	
	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip; 
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		
	    BlockPos pos = accessor.getPosition();
	    World world = accessor.getWorld();

	    IBlockState bs = world.getBlockState(pos);
	    Block block = bs.getBlock();
	    TileEntity te = world.getTileEntity(pos);
	    
	    ITooltipHint hint = null;
	    
	    if (te instanceof ITooltipHint) {
	    		hint = (ITooltipHint)te;
	    } else if (block instanceof ITooltipHint) {
	    		hint = (ITooltipHint)block;
	    }

	    if (hint != null) {
	    		String tooltip = hint.getTooltipHint(itemStack);
	    		
			String[] strs = tooltip.split("\n");
			
			for (String line : strs)
				currenttip.add(line);
				
	    }
	    
		return currenttip; 
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip; 
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
			BlockPos pos) {
		// TODO Auto-generated method stub
		return null;
	}

}
