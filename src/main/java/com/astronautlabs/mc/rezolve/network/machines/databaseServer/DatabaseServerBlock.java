package com.astronautlabs.mc.rezolve.network.machines.databaseServer;

import java.util.ArrayList;
import java.util.List;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.TileBlockBase;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import com.google.common.base.Predicate;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DatabaseServerBlock extends TileBlockBase {

	public DatabaseServerBlock() {
		super("block_database_server");
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public void registerRecipes() {
		
		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
			RecipeUtil.add(
				new ItemStack(this.itemBlock), 
				"nen",
				"CMC",
				"cac", 

				'n', Items.NAME_TAG,
				'e', Items.ENDER_EYE,
				'C', Blocks.CHEST,
				'M', Item.REGISTRY.getObject(new ResourceLocation("enderio:itemMachinePart")),
				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
				'a', Blocks.ANVIL
			);
		} else {
			RecipeUtil.add(
				new ItemStack(this.itemBlock), 
				"ene",
				"CpC",
				"cac", 
				
				'e', Items.ENDER_EYE,
				'n', Items.NAME_TAG,
				'C', Blocks.CHEST,
				'p', Items.PRISMARINE_SHARD,
				'c', RezolveMod.ETHERNET_CABLE_BLOCK,
				'a', Blocks.ANVIL
			);
		}
	}
	
	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return DatabaseServerEntity.class;
	}

	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal();
	};
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.VALUES[meta];
		
		if (!FACING.getAllowedValues().contains(facing)) {
			for (EnumFacing value : FACING.getAllowedValues()) {
				facing = value;
				break;
			}
		}
			
		return this.blockState.getBaseState()
			.withProperty(FACING, facing);
	}
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", new Predicate<EnumFacing>() {
		@Override
		public boolean apply(EnumFacing input) {
			return input != EnumFacing.DOWN && input != EnumFacing.UP;
		}
	});
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

		System.out.println("GETDROPS");
		
		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
		DatabaseServerEntity entity = (DatabaseServerEntity)world.getTileEntity(pos);
		
		if (entity != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			
			// Save the block entity state in the item. ItemBlock automatically restores this when the block item 
			// is replaced on the ground.
			
			NBTTagCompound blockEntityTag = new NBTTagCompound();
			entity.writeToNBT(blockEntityTag);
			nbt.setTag("BlockEntityTag", blockEntityTag);
			
			stack.setTagCompound(nbt);
		}
		
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(stack);
		return stacks;
	}
	
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te,
			ItemStack tool) {
		
		System.out.println("HARVEST");
		ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
		
		if (te != null) {
			DatabaseServerEntity dbEntity = (DatabaseServerEntity)te;
			
			NBTTagCompound nbt = new NBTTagCompound();
			
			// Save the block entity state in the item. ItemBlock automatically restores this when the block item 
			// is replaced on the ground.
			
			NBTTagCompound blockEntityTag = new NBTTagCompound();
			dbEntity.writeToNBT(blockEntityTag);
			nbt.setTag("BlockEntityTag", blockEntityTag);

			System.out.println("SETTING NBT");
			stack.setTagCompound(nbt);
		}

		System.out.println("SPAWNIN");
		spawnAsEntity(worldIn, pos, stack);
	}

}
