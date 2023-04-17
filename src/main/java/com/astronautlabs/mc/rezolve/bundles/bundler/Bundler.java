package com.astronautlabs.mc.rezolve.bundles.bundler;

import com.astronautlabs.mc.rezolve.common.machines.Machine;

import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

@RegistryId("bundler")
@WithBlockEntity(BundlerEntity.class)
@WithMenu(BundlerMenu.class)
public class Bundler extends Machine {
	public Bundler() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

		return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
	}
}
