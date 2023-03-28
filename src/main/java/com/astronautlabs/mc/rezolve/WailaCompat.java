//package com.astronautlabs.mc.rezolve;
//
//import java.util.List;
//
//import com.astronautlabs.mc.rezolve.common.BlockBase;
//import com.astronautlabs.mc.rezolve.common.ITooltipHint;
//
//import mcp.mobius.waila.api.IWailaConfigHandler;
//import mcp.mobius.waila.api.IWailaDataAccessor;
//import mcp.mobius.waila.api.IWailaDataProvider;
//import mcp.mobius.waila.api.IWailaRegistrar;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.Level;
//
//public class WailaCompat implements IWailaDataProvider {
//	public static final WailaCompat INSTANCE = new WailaCompat();
//
//	public static void load(IWailaRegistrar registrar) {
//		System.out.println("Bundler: Waila Registration....");
//		//registrar.registerHeadProvider(INSTANCE, BlockBase.class);
//		registrar.registerBodyProvider(INSTANCE, BlockBase.class);
//		registrar.registerBodyProvider(INSTANCE, BlockBase.class);
//		//registrar.registerNBTProvider(INSTANCE, TileEntityBase.class);
//	}
//
//	@Override
//	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
//			IWailaConfigHandler config) {
//		return currenttip;
//	}
//
//	@Override
//	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
//			IWailaConfigHandler config) {
//
//	    BlockPos pos = accessor.getPosition();
//	    Level world = accessor.getWorld();
//
//	    BlockState bs = world.getBlockState(pos);
//	    Block block = bs.getBlock();
//	    BlockEntity te = world.getBlockEntity(pos);
//
//	    ITooltipHint hint = null;
//
//	    if (te instanceof ITooltipHint) {
//	    		hint = (ITooltipHint)te;
//	    } else if (block instanceof ITooltipHint) {
//	    		hint = (ITooltipHint)block;
//	    }
//
//	    if (hint != null) {
//	    		String tooltip = hint.getTooltipHint(itemStack);
//
//			String[] strs = tooltip.split("\n");
//
//			for (String line : strs)
//				currenttip.add(line);
//
//	    }
//
//		return currenttip;
//	}
//
//	@Override
//	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
//			IWailaConfigHandler config) {
//		return currenttip;
//	}
//
//	@Override
//	public CompoundTag getNBTData(EntityPlayerMP player, BlockEntity te, CompoundTag tag, Level world,
//			BlockPos pos) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
