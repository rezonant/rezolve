package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import com.astronautlabs.mc.rezolve.common.blocks.BlockBase;

import com.astronautlabs.mc.rezolve.common.machines.Machine;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class Cable extends Machine {
	public Cable(Properties properties) {
		super(properties.isViewBlocking((state, level, pos) -> false).noOcclusion());
	}

	public boolean canNetworkWith(BlockGetter w, BlockPos thisBlock, BlockState bs, Direction face, BlockPos otherBlock) {
		return false;
	}
}