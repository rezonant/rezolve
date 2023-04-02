package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.thunderbolt.databaseServer.DatabaseServerEntity;
import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.SecurityServerEntity;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class CableNetwork {
	public CableNetwork(BlockGetter world, BlockPos startingPoint, Cable cableType) {
		this.world = world;
		this.startingPoint = startingPoint;
		this.cableType = cableType;
	}
	
	private BlockGetter world;
	private BlockPos startingPoint;
	private Cable cableType;
	
	private Direction[] getSurroundingPositions() {
		return new Direction[] {
			Direction.NORTH,
			Direction.EAST,
			Direction.WEST,
			Direction.DOWN,
			Direction.UP,
			Direction.SOUTH
		};
	}
	
	public BlockPos[] getNextCables(BlockGetter world, BlockPos pos, BlockPos fromBlock) {
		BlockState thisBlock = world.getBlockState(pos);
		
		ArrayList<BlockPos> viableCables = new ArrayList<BlockPos>();
		for (Direction dir : this.getSurroundingPositions()) {
			BlockPos adj = pos.relative(dir);
			
			if (fromBlock != null && adj.equals(fromBlock))
				continue;

			Cable cableType = this.cableType;
			if (thisBlock.getBlock() instanceof Cable)
				cableType = (Cable)thisBlock.getBlock();

			if (!cableType.canNetworkWith(world, pos, thisBlock, dir, adj))
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
			BlockEntity entity = this.world.getBlockEntity(pos);
			
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
			BlockEntity entity = this.world.getBlockEntity(pos);
			
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
					BlockState cableBlockState = this.world.getBlockState(cable);
					
					if (!this.cableType.getClass().isAssignableFrom(cableBlockState.getBlock().getClass())) {
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
