package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveMod;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

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

	float radius = 3f / 16f;

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		this.notifyNetwork(worldIn, pos);
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
		this.notifyNetwork(worldIn, pos);
		return super.canPlaceBlockOnSide(worldIn, pos, side);
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		this.notifyNetwork(world, pos);		
		super.onBlockAdded(world, pos, state);
	}
	
	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
		this.onBlockDestroyed(world, pos);
		super.onBlockDestroyedByPlayer(world, pos, state);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosionIn) {
		this.onBlockDestroyed(world, pos);
		super.onBlockDestroyedByExplosion(world, pos, explosionIn);
	}
	
	private void onBlockDestroyed(World world, BlockPos pos)
	{
		this.notifyNetwork(world, pos);
	}
	
	/**
	 * Update the cable network if we're on the server
	 * 
	 * @param world
	 * @param pos
	 */
	private void notifyNetwork(World world, BlockPos pos) {
		// Update the cable network if we're on the server
		
		if (world.isRemote)
			return;
		
		CableNetwork network = new CableNetwork(world, pos, RezolveMod.ETHERNET_CABLE_BLOCK);
		
		for (BlockPos otherPos : network.getEndpoints()) {
			TileEntity entity = world.getTileEntity(otherPos);
			if (entity == null)
				continue;
			
			if (entity instanceof ICableEndpoint) {
				((ICableEndpoint)entity).onCableUpdate();
			}
		}
	}
	
	public static final PropertyBool DOWN = PropertyBool.create("down");
	public static final PropertyBool EAST = PropertyBool.create("east");
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool WEST = PropertyBool.create("west");

	@Override
	public IBlockState getStateFromMeta(int p_getStateFromMeta_1_) {
		return this.getDefaultState();
	}
	
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
	public IBlockState getActualState(final IBlockState bs, final IBlockAccess world, final BlockPos coord) {
		IBlockState oldBS = bs;
		return bs
				.withProperty(WEST, this.canConnectTo(world, coord, oldBS, EnumFacing.WEST, coord.west()))
				.withProperty(DOWN, this.canConnectTo(world, coord, oldBS, EnumFacing.DOWN, coord.down()))
				.withProperty(SOUTH, this.canConnectTo(world, coord, oldBS, EnumFacing.SOUTH, coord.south()))
				.withProperty(EAST, this.canConnectTo(world, coord, oldBS, EnumFacing.EAST, coord.east()))
				.withProperty(UP, this.canConnectTo(world, coord, oldBS, EnumFacing.UP, coord.up()))
				.withProperty(NORTH, this.canConnectTo(world, coord, oldBS, EnumFacing.NORTH, coord.north()));
	}
	
	public boolean canConnectTo(IBlockAccess world, BlockPos pos) {
		IBlockState st = world.getBlockState(pos);
		TileEntity te = world.getTileEntity(pos);
		
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
