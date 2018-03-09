package com.astronautlabs.mc.rezolve.cable;

import com.astronautlabs.mc.rezolve.common.BlockBase;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CableBlock extends BlockBase {

	public CableBlock(String registryName, Material material, float hardness, float resistance) {
		super(registryName, material, hardness, resistance);
	}

	public CableBlock(String unlocalizedName, float hardness, float resistance) {
		super(unlocalizedName, hardness, resistance);
	}

	public CableBlock(String unlocalizedName) {
		super(unlocalizedName);
	}
	
	public boolean canConnectTo(IBlockAccess w, BlockPos thisBlock, IBlockState bs, EnumFacing face, BlockPos otherBlock) {
		return false;
	}
}