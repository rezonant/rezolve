package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.common.machines.Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class Cable extends Machine {
	public Cable(Properties properties) {
		super(properties.isViewBlocking((state, level, pos) -> false).noOcclusion());
	}

	public boolean canNetworkWith(BlockGetter w, BlockPos otherBlock) {
		return false;
	}
}