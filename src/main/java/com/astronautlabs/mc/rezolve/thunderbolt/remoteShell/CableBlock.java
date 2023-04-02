package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.common.blocks.BlockBase;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class CableBlock extends BlockBase {
	public CableBlock(Properties properties) {
		super(properties.isViewBlocking((state, level, pos) -> false).noOcclusion());
	}

	public boolean canConnectTo(BlockGetter w, BlockPos thisBlock, BlockState bs, Direction face, BlockPos otherBlock) {
		return false;
	}
}