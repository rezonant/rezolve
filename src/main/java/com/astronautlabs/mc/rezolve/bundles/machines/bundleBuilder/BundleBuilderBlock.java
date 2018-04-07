package com.astronautlabs.mc.rezolve.bundles.machines.bundleBuilder;

import com.astronautlabs.mc.rezolve.core.ModBase;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import com.astronautlabs.mc.rezolve.worlds.ores.Metal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BundleBuilderBlock extends Machine implements ITooltipHint {
	public BundleBuilderBlock() {
		super("block_bundle_builder");
	}
	
	@Override
	public void init(ModBase mod) {
		super.init(mod);
		BundleBuilderUpdateMessageHandler.register();
	}
	
	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return BundleBuilderEntity.class;
	}
	
	@Override
	public void registerRecipes() {
		RecipeUtil.add(new ItemStack(this.itemBlock),
			"cCc",
			"FMF",
			"cic",

			'c', RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.COPPER),
			'C', "mc:comparator",
			'F', "item_bundle_pattern|blank",
			'M', "block_machine_frame",
			'i', "item_machine_part|integrated_circuit"
		);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (stack.hasDisplayName()) {
	        ((BundleBuilderEntity) worldIn.getTileEntity(pos)).setCustomName(stack.getDisplayName());
	    }
		
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new BundleBuilderGuiContainer(player.inventory, (BundleBuilderEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}
	
	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new BundleBuilderContainer(player.inventory, (BundleBuilderEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new BundleBuilderEntity();
	}

	@Override
	public String getTooltipHint(ItemStack itemStack) {
		// TODO Auto-generated method stub
		return "I am a Bundle Builder!";
	}
	
}
