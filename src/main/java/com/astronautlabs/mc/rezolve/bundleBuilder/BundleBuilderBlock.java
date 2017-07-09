package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BundleBuilderBlock extends Machine implements ITooltipHint {
	public BundleBuilderBlock() {
		super("block_bundle_builder");
	}
	
	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		BundleBuilderUpdateMessageHandler.register();
	}
	
	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return BundleBuilderEntity.class;
	}
	
	@Override
	public void registerRecipes() {
		
		GameRegistry.addRecipe(new ItemStack(this.itemBlock), 
			"QEQ",
			"CRC",
			"QNQ", 
			
			'Q', Item.REGISTRY.getObject(new ResourceLocation("minecraft:quartz_block")),
			'E', Item.REGISTRY.getObject(new ResourceLocation("minecraft:enchanting_table")),
			'C', Item.REGISTRY.getObject(new ResourceLocation("minecraft:crafting_table")),
			'R', Item.REGISTRY.getObject(new ResourceLocation("minecraft:comparator")),
			'N', Item.REGISTRY.getObject(new ResourceLocation("minecraft:nether_star"))
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
