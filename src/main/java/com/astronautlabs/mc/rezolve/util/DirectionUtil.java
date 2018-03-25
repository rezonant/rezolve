package com.astronautlabs.mc.rezolve.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DirectionUtil {
	public static EnumFacing closestFace(BlockPos subjectBlock, BlockPos otherBlock) {

		if (otherBlock.getZ() < subjectBlock.getZ())
			return EnumFacing.NORTH;
		if (otherBlock.getZ() > subjectBlock.getZ())
			return EnumFacing.SOUTH;

		if (otherBlock.getX() < subjectBlock.getX())
			return EnumFacing.WEST;
		if (otherBlock.getX() > subjectBlock.getX())
			return EnumFacing.EAST;

		if (otherBlock.getY() < subjectBlock.getY())
			return EnumFacing.DOWN;

		if (otherBlock.getY() > subjectBlock.getY())
			return EnumFacing.UP;

		return EnumFacing.UP;
	}
}
