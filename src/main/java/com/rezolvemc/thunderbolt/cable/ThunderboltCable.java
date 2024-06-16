package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RegistryId("thunderbolt_cable")
@WithBlockEntity(ThunderboltCableEntity.class)
@WithMenu(ThunderboltCableMenu.class)
public class ThunderboltCable extends Cable {
	public static final BooleanProperty CONNECTS_DOWN = BooleanProperty.create("down");
	public static final BooleanProperty CONNECTS_EAST = BooleanProperty.create("east");
	public static final BooleanProperty CONNECTS_NORTH = BooleanProperty.create("north");
	public static final BooleanProperty CONNECTS_SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty CONNECTS_UP = BooleanProperty.create("up");
	public static final BooleanProperty CONNECTS_WEST = BooleanProperty.create("west");

	public static final BooleanProperty INTERFACES_DOWN = BooleanProperty.create("interfaces_down");
	public static final BooleanProperty INTERFACES_EAST = BooleanProperty.create("interfaces_east");
	public static final BooleanProperty INTERFACES_NORTH = BooleanProperty.create("interfaces_north");
	public static final BooleanProperty INTERFACES_SOUTH = BooleanProperty.create("interfaces_south");
	public static final BooleanProperty INTERFACES_UP = BooleanProperty.create("interfaces_up");
	public static final BooleanProperty INTERFACES_WEST = BooleanProperty.create("interfaces_west");

	public ThunderboltCable() {
		super(
				BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
						.isViewBlocking((state, level, pos) -> false)
						.noOcclusion()
				// TODO: no torches

		);
	}

	@Override
	protected void setupDefaultState() {
		this.registerDefaultState(
				this.defaultBlockState()
						.setValue(CONNECTS_DOWN, false)
						.setValue(CONNECTS_EAST, false)
						.setValue(CONNECTS_NORTH, false)
						.setValue(CONNECTS_SOUTH, false)
						.setValue(CONNECTS_UP, false)
						.setValue(CONNECTS_WEST, false)
						.setValue(INTERFACES_DOWN, false)
						.setValue(INTERFACES_EAST, false)
						.setValue(INTERFACES_NORTH, false)
						.setValue(INTERFACES_SOUTH, false)
						.setValue(INTERFACES_UP, false)
						.setValue(INTERFACES_WEST, false)
		);
	}

	private static final VoxelShape CENTER_SHAPE = Block.box(7, 7, 7, 9, 9, 9);

	private static final VoxelShape UP_SHAFT_SHAPE = Block.box(7, 9, 7, 9, 16, 9);
	private static final VoxelShape DOWN_SHAFT_SHAPE = Block.box(7, 0, 7, 9, 9, 9);
	private static final VoxelShape EAST_SHAFT_SHAPE = Block.box(7, 7, 7, 16, 9, 9);
	private static final VoxelShape WEST_SHAFT_SHAPE = Block.box(0, 7, 7, 7, 9, 9);
	private static final VoxelShape NORTH_SHAFT_SHAPE = Block.box(7, 7, 0, 9, 9, 9);
	private static final VoxelShape SOUTH_SHAFT_SHAPE = Block.box(7, 7, 7, 9, 9, 16);

	private static final int H_LEFT = 5;
	private static final int H_RIGHT = 11;
	private static final int H_TOP = 10;
	private static final int H_BOTTOM = 6;

	private static final VoxelShape NORTH_PLATE_SHAPE = Block.box(H_LEFT, H_BOTTOM, 0, H_RIGHT, H_TOP, 1);
	private static final VoxelShape SOUTH_PLATE_SHAPE = Block.box(H_LEFT, H_BOTTOM, 15, H_RIGHT, H_TOP, 16);
	private static final VoxelShape EAST_PLATE_SHAPE = Block.box(15, H_BOTTOM, H_LEFT, 16, H_TOP, H_RIGHT);
	private static final VoxelShape WEST_PLATE_SHAPE = Block.box(0, H_BOTTOM, H_LEFT, 1, H_TOP, H_RIGHT);
	private static final VoxelShape UP_PLATE_SHAPE = Block.box(H_BOTTOM, 15, H_LEFT, H_TOP, 16, H_RIGHT);
	private static final VoxelShape DOWN_PLATE_SHAPE = Block.box(H_BOTTOM, 0, H_LEFT, H_TOP, 1, H_RIGHT);

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		List<VoxelShape> shapes = new ArrayList<>();

		if (pState.getValue(CONNECTS_UP) || pState.getValue(INTERFACES_UP)) shapes.add(UP_SHAFT_SHAPE);
		if (pState.getValue(CONNECTS_DOWN) || pState.getValue(INTERFACES_DOWN)) shapes.add(DOWN_SHAFT_SHAPE);
		if (pState.getValue(CONNECTS_NORTH) || pState.getValue(INTERFACES_NORTH)) shapes.add(NORTH_SHAFT_SHAPE);
		if (pState.getValue(CONNECTS_SOUTH) || pState.getValue(INTERFACES_SOUTH)) shapes.add(SOUTH_SHAFT_SHAPE);
		if (pState.getValue(CONNECTS_EAST) || pState.getValue(INTERFACES_EAST)) shapes.add(EAST_SHAFT_SHAPE);
		if (pState.getValue(CONNECTS_WEST) || pState.getValue(INTERFACES_WEST)) shapes.add(WEST_SHAFT_SHAPE);

		if (pState.getValue(INTERFACES_UP)) shapes.add(UP_PLATE_SHAPE);
		if (pState.getValue(INTERFACES_DOWN)) shapes.add(DOWN_PLATE_SHAPE);
		if (pState.getValue(INTERFACES_NORTH)) shapes.add(NORTH_PLATE_SHAPE);
		if (pState.getValue(INTERFACES_SOUTH)) shapes.add(SOUTH_PLATE_SHAPE);
		if (pState.getValue(INTERFACES_EAST)) shapes.add(EAST_PLATE_SHAPE);
		if (pState.getValue(INTERFACES_WEST)) shapes.add(WEST_PLATE_SHAPE);

		VoxelShape[] shapesArray = new VoxelShape[shapes.size()];
		shapes.toArray(shapesArray);
		return Shapes.or(CENTER_SHAPE, shapesArray);
	}

	float radius = 3f / 16f;

	private static final Logger LOGGER = LogManager.getLogger(Rezolve.ID);

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

		var plates = Map.of(
				Direction.UP, UP_PLATE_SHAPE,
				Direction.DOWN, DOWN_PLATE_SHAPE,
				Direction.NORTH, NORTH_PLATE_SHAPE,
				Direction.SOUTH, SOUTH_PLATE_SHAPE,
				Direction.EAST, EAST_PLATE_SHAPE,
				Direction.WEST, WEST_PLATE_SHAPE
		);

		boolean hit = false;
		Direction hitDirection = Direction.UP;

		for (var entry : plates.entrySet()) {
			if (entry.getValue().bounds().move(pPos).inflate(0.01).contains(pHit.getLocation())) {
				LOGGER.info("Hit the {} plate!", entry.getKey());
				hitDirection = entry.getKey();
				hit = true;
			}
		}

		if (hit) {
			if (!pLevel.isClientSide && pPlayer instanceof ServerPlayer serverPlayer) {
				var entity = ((ThunderboltCableEntity)pLevel.getBlockEntity(pPos));
				final var direction = hitDirection;
				NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
						(containerId, playerInventory, player) -> {
							var menu = (ThunderboltCableMenu)createMenu(containerId, playerInventory, entity);
							if (direction == null) {
								throw new RuntimeException("Direction should never be null");
							}
							menu.direction = direction;
							menu.updateState();
							return menu;
						},
						entity.getMenuTitle()
				));
			}

			return InteractionResult.sidedSuccess(pLevel.isClientSide);
		}

		return InteractionResult.PASS;
	}

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
		super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
		this.notifyNetwork(pLevel, pPos);

		var entity = pLevel.getBlockEntity(pPos);
		if (entity instanceof ThunderboltCableEntity cableEntity) {
			cableEntity.onNeighborChanged(pFromPos);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder
				.add(CONNECTS_DOWN)
				.add(CONNECTS_EAST)
				.add(CONNECTS_NORTH)
				.add(CONNECTS_SOUTH)
				.add(CONNECTS_UP)
				.add(CONNECTS_WEST)
				.add(INTERFACES_DOWN)
				.add(INTERFACES_EAST)
				.add(INTERFACES_NORTH)
				.add(INTERFACES_SOUTH)
				.add(INTERFACES_UP)
				.add(INTERFACES_WEST)
		;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return getShapeForPosition(defaultBlockState(), pContext.getClickedPos(), pContext.getLevel());
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

	private void onBlockDestroyed(Level world, BlockPos pos) {
		this.notifyNetwork(world, pos);
	}

	/**
	 * Update the cable network if we're on the server
	 *
	 * @param world
	 * @param pos
	 */
	private void notifyNetwork(Level world, BlockPos pos) {
		if (world.isClientSide)
			return;

		if (world.getBlockEntity(pos) instanceof ThunderboltCableEntity cableEntity) {
			for (var endpoint : cableEntity.getNetwork().getEndpoints()) {
				if (endpoint.getBlockEntity() instanceof ICableEndpoint cableEndpoint) {
					cableEndpoint.onCableUpdate();
				}
			}
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> GameEventListener getListener(ServerLevel pLevel, T pBlockEntity) {
		return pBlockEntity instanceof ThunderboltCableEntity ? (ThunderboltCableEntity)pBlockEntity : null;
	}

	@Override
	public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
		super.destroy(pLevel, pPos, pState);
	}

	@Override
	public BlockState updateShape(BlockState oldBS, Direction pDirection, BlockState pNeighborState, LevelAccessor level, BlockPos pos, BlockPos pNeighborPos) {
		return getShapeForPosition(oldBS, pos, level);
	}

	private BlockState getShapeForPosition(BlockState baseState, BlockPos pos, LevelAccessor level) {
		return baseState

				// "connects" properties: true when the cable touches another cable

				.setValue(CONNECTS_WEST, getConnectionType(level, pos, baseState, Direction.WEST, pos.west()) == ConnectionType.CONNECTS)
				.setValue(CONNECTS_DOWN, getConnectionType(level, pos, baseState, Direction.DOWN, pos.below()) == ConnectionType.CONNECTS)
				.setValue(CONNECTS_SOUTH, getConnectionType(level, pos, baseState, Direction.SOUTH, pos.south()) == ConnectionType.CONNECTS)
				.setValue(CONNECTS_EAST, getConnectionType(level, pos, baseState, Direction.EAST, pos.east()) == ConnectionType.CONNECTS)
				.setValue(CONNECTS_UP, getConnectionType(level, pos, baseState, Direction.UP, pos.above()) == ConnectionType.CONNECTS)
				.setValue(CONNECTS_NORTH, getConnectionType(level, pos, baseState, Direction.NORTH, pos.north()) == ConnectionType.CONNECTS)

				// "interfaces" properties: true when the cable touches a non-cable block that it can interface with

				.setValue(INTERFACES_WEST, getConnectionType(level, pos, baseState, Direction.WEST, pos.west()) == ConnectionType.INTERFACES)
				.setValue(INTERFACES_DOWN, getConnectionType(level, pos, baseState, Direction.DOWN, pos.below()) == ConnectionType.INTERFACES)
				.setValue(INTERFACES_SOUTH, getConnectionType(level, pos, baseState, Direction.SOUTH, pos.south()) == ConnectionType.INTERFACES)
				.setValue(INTERFACES_EAST, getConnectionType(level, pos, baseState, Direction.EAST, pos.east()) == ConnectionType.INTERFACES)
				.setValue(INTERFACES_UP, getConnectionType(level, pos, baseState, Direction.UP, pos.above()) == ConnectionType.INTERFACES)
				.setValue(INTERFACES_NORTH, getConnectionType(level, pos, baseState, Direction.NORTH, pos.north()) == ConnectionType.INTERFACES)
		;
	}

	public boolean canConnectTo(BlockGetter world, BlockPos pos) {
		BlockState st = world.getBlockState(pos);

		if (st.getBlock() == Blocks.NETHER_PORTAL)
			return true;

		return st != null && st.getBlock() == this;
	}

	/**
	 * Returns true if Thunderbolt cable can interface with the given block (that is,
	 * the block is a valid "endpoint"). This returns false for cables.
	 * @param world
	 * @param pos
	 * @return
	 */
	public boolean canInterfaceWith(BlockGetter world, BlockPos pos) {
		BlockState st = world.getBlockState(pos);
		BlockEntity te = world.getBlockEntity(pos);

		// Cables (specifically) cannot be interfaced with.
		if (st.getBlock() == this)
			return false;

		if (st == null || st.getBlock() == null)
			return false;

		if (st.getBlock() == Blocks.ANVIL || te != null)
			return true;

		return false;
	}

    @Override
    public boolean canNetworkWith(BlockGetter w, BlockPos otherBlock) {
        return canInterfaceWith(w, otherBlock) || canConnectTo(w, otherBlock);
    }

    enum ConnectionType {
		NONE,
		INTERFACES,
		CONNECTS
	}

	public ConnectionType getConnectionType(BlockGetter w, BlockPos thisBlock, BlockState bs, Direction face, BlockPos otherBlock) {
		BlockState st = w.getBlockState(otherBlock);

		if (canConnectTo(w, otherBlock))
			return ConnectionType.CONNECTS;
		else if (canInterfaceWith(w, otherBlock))
			return ConnectionType.INTERFACES;

		return ConnectionType.NONE;
	}
}
