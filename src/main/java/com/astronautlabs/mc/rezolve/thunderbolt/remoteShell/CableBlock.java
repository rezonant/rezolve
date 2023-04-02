package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.common.blocks.BlockBase;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class CableBlock extends BlockBase {
	public CableBlock(Properties properties) {
		super(properties.isViewBlocking((state, level, pos) -> false).noOcclusion());
	}

	public boolean canNetworkWith(BlockGetter w, BlockPos thisBlock, BlockState bs, Direction face, BlockPos otherBlock) {
		return false;
	}
}