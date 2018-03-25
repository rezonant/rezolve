package com.astronautlabs.mc.rezolve.bundles.machines.bundleBuilder;

import com.astronautlabs.mc.rezolve.ModBase;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
			RecipeUtil.add(new ItemStack(this.itemBlock),
				"RCR",
				"FMF",
				"RcR", 

				'R', "item|enderio:itemAlloy|3",
				'C', Items.COMPARATOR,
				'F', "item|enderio:itemBasicFilterUpgrade",
				'M', "item|enderio:itemMachinePart|0",
				'c', "item|enderio:itemBasicCapacitor|2"
			);
		} else {
			RecipeUtil.add(new ItemStack(this.itemBlock),
				"QEQ",
				"CRC",
				"QNQ", 
				
				'Q', Blocks.QUARTZ_BLOCK,
				'E', Blocks.ENCHANTING_TABLE,
				'C', Blocks.CRAFTING_TABLE,
				'R', Items.COMPARATOR,
				'N', Items.NETHER_STAR
			);
		}
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
