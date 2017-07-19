package com.astronautlabs.mc.rezolve.remoteShell;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.databaseServer.DatabaseServerEntity;
import com.astronautlabs.mc.rezolve.securityServer.SecurityServerEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CableNetwork {
	public CableNetwork(IBlockAccess world, BlockPos startingPoint, CableBlock cableType) {
		this.world = world;
		this.startingPoint = startingPoint;
		this.cableType = cableType;
	}
	
	private IBlockAccess world;
	private BlockPos startingPoint;
	private CableBlock cableType;
	
	private EnumFacing[] getSurroundingPositions() {
		return new EnumFacing[] {
			EnumFacing.NORTH,
			EnumFacing.EAST,
			EnumFacing.WEST,
			EnumFacing.DOWN,
			EnumFacing.UP,
			EnumFacing.SOUTH
		};
	}
	
	public BlockPos[] getNextCables(IBlockAccess world, BlockPos pos, BlockPos fromBlock) {
		IBlockState thisBlock = world.getBlockState(pos);
		
		ArrayList<BlockPos> viableCables = new ArrayList<BlockPos>();
		for (EnumFacing dir : this.getSurroundingPositions()) {
			BlockPos adj = pos.offset(dir);
			
			if (fromBlock != null && adj.equals(fromBlock))
				continue;
			
			if (!this.cableType.canConnectTo(world, pos, thisBlock, dir, adj))
				continue;

			viableCables.add(adj);
		}
		
		return viableCables.toArray(new BlockPos[viableCables.size()]);
	}
	
	public SecurityServerEntity getSecurityServer() {
		return this.getSecurityServer(this.getEndpoints());
	}
	
	public SecurityServerEntity getSecurityServer(BlockPos[] endpoints) {
		for (BlockPos pos : endpoints) {
			TileEntity entity = this.world.getTileEntity(pos);
			
			if (entity instanceof SecurityServerEntity) 
				return (SecurityServerEntity)entity;
		}
		
		return null;
	}
	
	public DatabaseServerEntity getDatabaseServer() {
		return this.getDatabaseServer(this.getEndpoints());
	}
	
	public DatabaseServerEntity getDatabaseServer(BlockPos[] endpoints) {
		for (BlockPos pos : endpoints) {
			TileEntity entity = this.world.getTileEntity(pos);
			
			if (entity instanceof DatabaseServerEntity) 
				return (DatabaseServerEntity)entity;
		}
		
		return null;
	}
	
	public BlockPos[] getEndpoints() {
		
		ArrayList<BlockPos> endpoints = new ArrayList<BlockPos>();
		ArrayList<BlockPos> visited = new ArrayList<BlockPos>();
		ArrayList<BlockPos> sources = new ArrayList<BlockPos>();

		BlockPos fromBlock = null;
		
		sources.add(this.startingPoint);
		
		while (true) {
			ArrayList<BlockPos> nextSources = new ArrayList<BlockPos>();
			
			for (BlockPos source : sources) {
				if (visited.contains(source))
					continue;
				
				BlockPos[] cables = this.getNextCables(this.world, source, fromBlock);
				
				visited.add(source);
				
				for (BlockPos cable : cables) {
					IBlockState cableBlockState = this.world.getBlockState(cable);
					
					if (cableBlockState.getBlock() != this.cableType) {
						if (!endpoints.contains(cable))
							endpoints.add(cable);
						
						continue;
					}
					
					if (visited.contains(cable))
						continue;
					
					nextSources.add(cable);
				}
				
				fromBlock = source;
			}
			
			if (nextSources.size() == 0)
				break;
			
			sources = nextSources;
		}
		
		return endpoints.toArray(new BlockPos[endpoints.size()]);
	}
}
