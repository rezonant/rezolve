package com.astronautlabs.mc.rezolve.network.cable;

import com.astronautlabs.mc.rezolve.RezolveMod;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import com.astronautlabs.mc.rezolve.worlds.ores.Metal;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class EthernetCableBlock extends CableBlock {

	public EthernetCableBlock() {
		super("block_ethernet_cable");
		
		this.setDefaultState(
			this.getDefaultState()
				.withProperty(DOWN, false)
				.withProperty(EAST, false)
				.withProperty(NORTH, false)
				.withProperty(SOUTH, false)
				.withProperty(UP, false)
				.withProperty(WEST, false)
		);
	}

	@Override
	public void registerRecipes() {
		RecipeUtil.add(
			new ItemStack(this.itemBlock, 4),
			"tWt",
			"WCW",
			"tWt",

			't', RezolveMod.METAL_NUGGET_ITEM.getStackOf(Metal.TIN),
			'W', Blocks.WOOL,
			'C', RezolveMod.METAL_INGOT_ITEM.getStackOf(Metal.COPPER)
		);
	}
	
	float radius = 3f / 16f;

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		//this.invalidateNetwork(worldIn, pos);
	}

	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(blockAccess, pos, neighbor);

		if (!(blockAccess instanceof World))
			return;

		World world = (World)blockAccess;

		// Update the cable network if we're on the server

		if (!world.isRemote) {
			CableNetwork network = this.networkAt(world, pos, false);
			if (network != null)
				network.endpointChanged(world, pos, neighbor);
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, DOWN, EAST, NORTH, SOUTH, UP, WEST);
	}
	
	@Override
	public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		return super.canPlaceBlockOnSide(worldIn, pos, side);
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {

		// Update the cable network if we're on the server

		if (!world.isRemote) {
			CableNetwork network = this.networkAt(world, pos, false);
			if (network != null)
				network.cableRemoved(world, pos);
		}

		super.onBlockDestroyedByPlayer(world, pos, state);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn) {
		// Update the cable network if we're on the server

		if (!world.isRemote) {
			CableNetwork network = this.networkAt(world, pos, false);
			if (network != null)
				network.cableRemoved(world, pos);
		}

		super.onBlockDestroyedByExplosion(world, pos, explosionIn);
	}

	public CableNetwork networkAt(World world, BlockPos pos, boolean create) {
		return CableNetwork.networkAt(world, pos, RezolveMod.ETHERNET_CABLE_BLOCK, create);
	}
	
	public static final PropertyBool DOWN = PropertyBool.create("down");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool WEST = PropertyBool.create("west");
	
	@Override
	public int getMetaFromState(IBlockState p_getMetaFromState_1_) {
		return 0;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState p_isOpaqueCube_1_) {
		return false;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	/**
	 * Sets the data values of a BlockState instance to represent this block
	 */
	@Override
	public IBlockState getActualState(final IBlockState bs, final IBlockAccess blockAccess, final BlockPos coord) {
		IBlockState oldBS = bs;

		return bs
			.withProperty(WEST,		this.canConnectTo(blockAccess, coord, oldBS, EnumFacing.WEST,		coord.west()))
			.withProperty(DOWN,		this.canConnectTo(blockAccess, coord, oldBS, EnumFacing.DOWN,		coord.down()))
			.withProperty(SOUTH,	this.canConnectTo(blockAccess, coord, oldBS, EnumFacing.SOUTH,	coord.south()))
			.withProperty(EAST,		this.canConnectTo(blockAccess, coord, oldBS, EnumFacing.EAST,		coord.east()))
			.withProperty(UP,		this.canConnectTo(blockAccess, coord, oldBS, EnumFacing.UP, 		coord.up()))
			.withProperty(NORTH,	this.canConnectTo(blockAccess, coord, oldBS, EnumFacing.NORTH,	coord.north()))
		;
	}
	
	public boolean canConnectTo(IBlockAccess blockAccess, BlockPos pos) {

		if (blockAccess instanceof World) {
			World world = (World)blockAccess;
			if (!world.isAreaLoaded(pos, 1))
				return false;
		}

		IBlockState st = blockAccess.getBlockState(pos);
		TileEntity te = blockAccess.getTileEntity(pos);
		
		if (st == null || st.getBlock() == null || "enderio:blockConduitBundle".equals(st.getBlock().getRegistryName().toString()))
			return false;
		
		if (st.getBlock() == RezolveMod.ETHERNET_CABLE_BLOCK || st.getBlock() == Blocks.ANVIL || te != null)
			return true;
		
		return false;
	}

	public boolean canConnectTo(IBlockAccess w, BlockPos thisBlock, IBlockState bs, EnumFacing face, BlockPos otherBlock) {
		return this.canConnectTo(w, otherBlock);
	}
	
	/**
	 * Calculates the collision boxes for this block.
	 */
	@Override
	public AxisAlignedBB getBoundingBox(final IBlockState bs, final IBlockAccess world, final BlockPos coord) {
		IBlockState oldBS = bs;
		final boolean connectNorth = this.canConnectTo(world, coord, oldBS, EnumFacing.NORTH, coord.north());
		final boolean connectSouth = this.canConnectTo(world, coord, oldBS, EnumFacing.SOUTH, coord.south());
		final boolean connectWest = this.canConnectTo(world, coord, oldBS, EnumFacing.WEST, coord.west());
		final boolean connectEast = this.canConnectTo(world, coord, oldBS, EnumFacing.EAST, coord.east());
		final boolean connectUp = this.canConnectTo(world, coord, oldBS, EnumFacing.UP, coord.up());
		final boolean connectDown = this.canConnectTo(world, coord, oldBS, EnumFacing.DOWN, coord.down());
		final boolean allFalse = !(connectNorth || connectSouth || connectWest || connectEast || connectUp || connectDown);

		float radius = this.radius;
		float rminus = 0.5f - radius;
		float rplus = 0.5f + radius;

		float x1 = rminus;
		float x2 = rplus;
		float y1 = rminus;
		float y2 = rplus;
		float z1 = rminus;
		float z2 = rplus;
		if (connectNorth) {
			z1 = 0.0f;
		}
		if (connectSouth) {
			z2 = 1.0f;
		}
		if (connectWest) {
			x1 = 0.0f;
		}
		if (connectEast) {
			x2 = 1.0f;
		}
		if (connectDown) {
			y1 = 0.0f;
		}
		if (connectUp) {
			y2 = 1.0f;
		}
		if (allFalse) {// Horizontal '+' when making no connections
			z1 = 0.0f;
			z2 = 1.0f;
			x1 = 0.0f;
			x2 = 1.0f;
		}
		return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
	}
}
