package com.rezolvemc.common.blocks;

import java.lang.reflect.Constructor;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineEntity;
import org.torchmc.WithMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class EntityBlockBase extends BlockBase implements EntityBlock {
	private static final Logger LOGGER = LogManager.getLogger();

	public EntityBlockBase(Properties properties) {
		super(properties);
	}

	@Override
	public void init(Rezolve mod) {
		super.init(mod);
	}

	protected Class<? extends AbstractContainerMenu> getMenuClass() {
		var annotation = this.getClass().getAnnotation(WithMenu.class);
		if (annotation == null)
			throw new RuntimeException(String.format("Block %s must either be annotated @WithMenu() or override getMenuClass()", getClass().getCanonicalName()));

		return annotation.value();
	}

	public Class<? extends BlockEntity> getBlockEntityClass() {
		var annotation = this.getClass().getAnnotation(WithBlockEntity.class);
		if (annotation == null)
			throw new RuntimeException(String.format("Block %s must either be annotated @WithBlockEntity() or reimplement getBlockEntityClass().", getClass().getCanonicalName()));

		return annotation.value();
	}

	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, MachineEntity entity) {
		try {
			return getMenuClass().getConstructor(int.class, Inventory.class, entity.getClass())
					.newInstance(containerId, playerInventory, entity);
		} catch (ReflectiveOperationException e) {
			LOGGER.error("Caught exception while creating menu:");
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     * 
     * PERFORMANCE NOTE: Default implementation relies on reflection, if performance is an issue (due to lots of 
     * tile entities being created at the same time) then this should be overridden to use 
     * a standard invocation.
     */
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		Class<? extends BlockEntity> klass = this.getBlockEntityClass();
		
		if (klass == null)
			return null;
		
		Constructor<? extends BlockEntity> ctor;
		BlockEntity instance;
		
		try {
			ctor = klass.getDeclaredConstructor(BlockPos.class, BlockState.class);
			instance = ctor.newInstance(pos, state);
		} catch (Exception e) {
			LOGGER.error("Cannot construct block entity {}: {}", klass.getCanonicalName(), e.getMessage());
			LOGGER.error(e);
			return null;
		}
		
		return instance;
	}

	public void openMenu(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
	{
		if (!pLevel.isClientSide && pPlayer instanceof ServerPlayer serverPlayer) {
			NetworkHooks.openScreen(serverPlayer, pState.getMenuProvider(pLevel, pPos));
		}
	}

//    @Override
//    public void breakBlock(Level world, BlockPos pos, BlockState state) {
//
//    	// Drop any items we have if necessary
//		BlockEntity te = world.getBlockEntity(pos);
//		if (te != null && te instanceof IInventory) {
//			InventoryHelper.dropInventoryItems(world, pos, (IInventory)te);
//		}
//
//        super.breakBlock(world, pos, state);
//        world.removeTileEntity(pos);
//    }
//
//    @Override
//    public boolean eventReceived(BlockState state, Level worldIn, BlockPos pos, int eventId, int eventParam) {
//        super.eventReceived(state, worldIn, pos, eventId, eventParam);
//        BlockEntity tileentity = worldIn.getBlockEntity(pos);
//        return tileentity == null ? false : tileentity.receiveClientEvent(eventId, eventParam);
//    }
}
