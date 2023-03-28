package com.astronautlabs.mc.rezolve.common;

import com.google.common.base.Predicate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class Machine extends EntityBlockBase {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final DirectionProperty FACING = DirectionProperty.create("facing", new Predicate<Direction>() {
		@Override
		public boolean apply(Direction input) {
			return input != Direction.DOWN && input != Direction.UP;
		}
	});

	public Machine(Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		if (pStack.hasCustomHoverName()) {
			BlockEntity entity = pLevel.getBlockEntity(pPos);
			if (entity != null && entity instanceof BlockEntityBase)
				((MachineEntity)entity).setCustomName(pStack.getHoverName().getString());
		}

		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
		var entity = ((MachineEntity)pLevel.getBlockEntity(pPos));
		return new SimpleMenuProvider(
				(containerId, playerInventory, player) -> createMenu(containerId, playerInventory, entity),
				entity.getMenuTitle()
		);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		this.openMenu(pState, pLevel, pPos, pPlayer);
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}
}
