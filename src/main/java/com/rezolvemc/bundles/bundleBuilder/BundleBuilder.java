package com.rezolvemc.bundles.bundleBuilder;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.ITooltipHint;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.blocks.BlockEntityBase;

import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

@RegistryId("bundle_builder")
@WithBlockEntity(BundleBuilderEntity.class)
@WithMenu(BundleBuilderMenu.class)
public class BundleBuilder extends Machine implements ITooltipHint {
	public BundleBuilder() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL));
	}


	@Override
	public void init(Rezolve mod) {
		super.init(mod);
		//BundleBuilderUpdateMessageHandler.register();
	}
	
	@Override
	public Class<? extends BlockEntityBase> getBlockEntityClass() {
		return BundleBuilderEntity.class;
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		if (pStack.hasCustomHoverName()) {
			((BundleBuilderEntity) pLevel.getBlockEntity(pPos)).setCustomName(pStack.getHoverName().getString());
		}

		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
	}

	@Override
	public String getTooltipHint(ItemStack itemStack) {
		return "I am a Bundle Builder!";
	}
	
}
