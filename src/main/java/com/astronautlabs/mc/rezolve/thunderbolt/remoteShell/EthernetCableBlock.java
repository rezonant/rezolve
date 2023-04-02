package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;

@RegistryId("ethernet_cable")
public class EthernetCableBlock extends CableBlock {
	public static final BooleanProperty DOWN = BooleanProperty.create("down");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty WEST = BooleanProperty.create("west");

	public EthernetCableBlock() {
		super(
				BlockBehaviour.Properties.of(Material.WOOL)
					.isViewBlocking((state, level, pos) -> false)
				// TODO: no torches

		);

		this.registerDefaultState(
				this.defaultBlockState()
						.setValue(DOWN, false)
						.setValue(EAST, false)
						.setValue(NORTH, false)
						.setValue(SOUTH, false)
						.setValue(UP, false)
						.setValue(WEST, false)
		);
	}

//	@Override
//	public void registerRecipes() {
//		ItemStack redstoneBundle = RezolveMod.BUNDLE_ITEM.withContents(
//			1,
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1),
//			new ItemStack(Items.REDSTONE, 1)
//		);
//
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"pWp",
//				"WRW",
//				"pWp",
//
//				'p', new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("enderio:itemMaterial")), 1, 3),
//				'W', Blocks.WOOL,
//				'R', redstoneBundle
//			);
//		} else {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"IWI",
//				"WRW",
//				"IWI",
//
//				'I', Items.IRON_INGOT,
//				'W', Blocks.WOOL,
//				'R', redstoneBundle
//			);
//		}
//	}
	
	float radius = 3f / 16f;

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
		super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
		this.notifyNetwork(pLevel, pPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(DOWN).add(EAST).add(NORTH).add(SOUTH).add(UP).add(WEST);
	}

	// TODO
//	@Override
//	public boolean canPlaceBlockOnSide(Level worldIn, BlockPos pos, Direction side) {
//		this.notifyNetwork(worldIn, pos);
//		return super.canPlaceBlockOnSide(worldIn, pos, side);
//	}
//
//	@Override
//	public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
//		this.notifyNetwork(world, pos);
//		super.onBlockAdded(world, pos, state);
//	}
//
//	@Override
//	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {
//		this.onBlockDestroyed(world, pos);
//		super.onBlockDestroyedByPlayer(world, pos, state);
//	}
//
//	@Override
//	public void onBlockDestroyedByExplosion(Level world, BlockPos pos, Explosion explosionIn) {
//		this.onBlockDestroyed(world, pos);
//		super.onBlockDestroyedByExplosion(world, pos, explosionIn);
//	}
	
	private void onBlockDestroyed(Level world, BlockPos pos)
	{
		this.notifyNetwork(world, pos);
	}
	
	/**
	 * Update the cable network if we're on the server
	 * 
	 * @param world
	 * @param pos
	 */
	private void notifyNetwork(Level world, BlockPos pos) {
		// Update the cable network if we're on the server
		
		if (world.isClientSide)
			return;
		
		CableNetwork network = new CableNetwork(world, pos, RezolveRegistry.block(EthernetCableBlock.class));
		
		for (BlockPos otherPos : network.getEndpoints()) {
			BlockEntity entity = world.getBlockEntity(otherPos);
			if (entity == null)
				continue;
			
			if (entity instanceof ICableEndpoint) {
				((ICableEndpoint)entity).onCableUpdate();
			}
		}
	}

//	/**
//	 * Sets the data values of a BlockState instance to represent this block
//	 */
//	@Override
//	public BlockState getActualState(final BlockState bs, final BlockGetter world, final BlockPos coord) {
//		BlockState oldBS = bs;
//		return bs
//				.withProperty(WEST, this.canConnectTo(world, coord, oldBS, Direction.WEST, coord.west()))
//				.withProperty(DOWN, this.canConnectTo(world, coord, oldBS, Direction.DOWN, coord.down()))
//				.withProperty(SOUTH, this.canConnectTo(world, coord, oldBS, Direction.SOUTH, coord.south()))
//				.withProperty(EAST, this.canConnectTo(world, coord, oldBS, Direction.EAST, coord.east()))
//				.withProperty(UP, this.canConnectTo(world, coord, oldBS, Direction.UP, coord.up()))
//				.withProperty(NORTH, this.canConnectTo(world, coord, oldBS, Direction.NORTH, coord.north()));
//	}


	@Override
	public BlockState updateShape(BlockState oldBS, Direction pDirection, BlockState pNeighborState, LevelAccessor level, BlockPos pos, BlockPos pNeighborPos) {
		return oldBS
				.setValue(WEST, this.canConnectTo(level, pos, oldBS, Direction.WEST, pos.west()))
				.setValue(DOWN, this.canConnectTo(level, pos, oldBS, Direction.DOWN, pos.above()))
				.setValue(SOUTH, this.canConnectTo(level, pos, oldBS, Direction.SOUTH, pos.south()))
				.setValue(EAST, this.canConnectTo(level, pos, oldBS, Direction.EAST, pos.east()))
				.setValue(UP, this.canConnectTo(level, pos, oldBS, Direction.UP, pos.below()))
				.setValue(NORTH, this.canConnectTo(level, pos, oldBS, Direction.NORTH, pos.north()));
	}

	public boolean canConnectTo(BlockGetter world, BlockPos pos) {
		BlockState st = world.getBlockState(pos);
		BlockEntity te = world.getBlockEntity(pos);
		
		if (st == null || st.getBlock() == null)
			return false;
		
		if (st.getBlock() == RezolveRegistry.block(EthernetCableBlock.class) || st.getBlock() == Blocks.ANVIL || te != null)
			return true;
		
		return false;
	}

	public boolean canConnectTo(BlockGetter w, BlockPos thisBlock, BlockState bs, Direction face, BlockPos otherBlock) {
		return this.canConnectTo(w, otherBlock);
	}

//	/**
//	 * Calculates the collision boxes for this block.
//	 */
//	@Override
//	public AABB getBoundingBox(final BlockState bs, final BlockGetter world, final BlockPos coord) {
//		BlockState oldBS = bs;
//		final boolean connectNorth = this.canConnectTo(world, coord, oldBS, Direction.NORTH, coord.north());
//		final boolean connectSouth = this.canConnectTo(world, coord, oldBS, Direction.SOUTH, coord.south());
//		final boolean connectWest = this.canConnectTo(world, coord, oldBS, Direction.WEST, coord.west());
//		final boolean connectEast = this.canConnectTo(world, coord, oldBS, Direction.EAST, coord.east());
//		final boolean connectUp = this.canConnectTo(world, coord, oldBS, Direction.UP, coord.up());
//		final boolean connectDown = this.canConnectTo(world, coord, oldBS, Direction.DOWN, coord.down());
//		final boolean allFalse = !(connectNorth || connectSouth || connectWest || connectEast || connectUp || connectDown);
//
//		float radius = this.radius;
//		float rminus = 0.5f - radius;
//		float rplus = 0.5f + radius;
//
//		float x1 = rminus;
//		float x2 = rplus;
//		float y1 = rminus;
//		float y2 = rplus;
//		float z1 = rminus;
//		float z2 = rplus;
//		if (connectNorth) {
//			z1 = 0.0f;
//		}
//		if (connectSouth) {
//			z2 = 1.0f;
//		}
//		if (connectWest) {
//			x1 = 0.0f;
//		}
//		if (connectEast) {
//			x2 = 1.0f;
//		}
//		if (connectDown) {
//			y1 = 0.0f;
//		}
//		if (connectUp) {
//			y2 = 1.0f;
//		}
//		if (allFalse) {// Horizontal '+' when making no connections
//			z1 = 0.0f;
//			z2 = 1.0f;
//			x1 = 0.0f;
//			x2 = 1.0f;
//		}
//		return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
//	}
}
