package com.rezolvemc.common.machines;

import com.rezolvemc.common.blocks.BlockEntityBase;
import com.rezolvemc.common.blocks.EntityBlockBase;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.google.common.base.Predicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
		setupDefaultState();
	}

	protected void setupDefaultState() {
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		if (pBlockEntityType != RezolveRegistry.blockEntityType(getBlockEntityClass()))
			return null;

		return (level, pos, state, entity) -> {
			if (entity instanceof MachineEntity machineEntity)
			machineEntity.tick();
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
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

	/**
	 * Tag to attach to an item when this block is broken and turned into an item
	 * @return
	 */
	public CompoundTag getItemTag() {
		return null;
	}

	@Override
	public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pBuilder) {
		var item = new ItemStack(asItem());
		item.setTag(getItemTag());
		return List.of(item);
	}
}
