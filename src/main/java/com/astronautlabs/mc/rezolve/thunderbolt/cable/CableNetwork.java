package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import java.util.*;

import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.thunderbolt.databaseServer.DatabaseServerEntity;
import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.SecurityServerEntity;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CableNetwork {
	private CableNetwork(BlockGetter level, BlockPos startingPoint, ThunderboltCable cableType) {
		this.level = level;
		this.startingPoint = startingPoint;
		this.cableType = cableType;
	}
	
	private BlockGetter level;
	private BlockPos startingPoint;
	private ThunderboltCable cableType;
	private boolean invalidated = false;
	private SecurityServerEntity securityServer;
	private DatabaseServerEntity databaseServer;
	private Map<BlockPos, CableEndpoint> endpoints = new HashMap<>();

	public static CableNetwork boot(BlockGetter world, BlockPos startingPoint) {
		return new CableNetwork(world, startingPoint, RezolveRegistry.block(ThunderboltCable.class))
				.boot();
	}

	private CableNetwork boot() {
		// First, discover all endpoints

		for (var endpoint : computeEndpoints())
			endpoints.put(endpoint.getPosition(), endpoint);

		// Second, have all cables and endpoints adopt the network,
		// now that we have complete endpoint information.
		// We need to wait to do this so that the cables can attach their
		// interface configuration information.

		visit((pos, state, entity) -> {
			if (entity instanceof MachineEntity machineEntity)
				machineEntity.adoptNetwork(this);
			return true;
		});

		// Find some special endpoints, like the security server/database server
		// etc.

		for (var endpoint : endpoints.values()) {
			var entity = this.level.getBlockEntity(endpoint.getPosition());

			if (entity instanceof SecurityServerEntity securityServer)
				this.securityServer = securityServer;

			if (entity instanceof DatabaseServerEntity databaseServer)
				this.databaseServer = databaseServer;
		}

		return this;
	}

	private List<CableEndpoint> computeEndpoints() {
		var endpointPositions = new HashSet<BlockPos>();

		visit((pos, state, entity) -> {
			if (cableType.canInterfaceWith(level, pos))
				endpointPositions.add(pos);
			return true;
		});

		return endpointPositions.stream()
				.map(position -> new CableEndpoint(position))
				.toList()
				;
	}
	public static CableNetwork[] getNetworksForEndpoint(Level level, BlockPos blockPos) {
		var networks = new ArrayList<CableNetwork>();

		for (var dir : Direction.values()) {
			var entity = level.getBlockEntity(blockPos.relative(dir));
			if (entity instanceof MachineEntity machineEntity && machineEntity.actsAsCable()) {
				networks.add(machineEntity.getNetwork());
			}
		}

		return networks.toArray(new CableNetwork[networks.size()]);
	}

	public boolean isInvalidated() {
		return invalidated;
	}

	public void invalidate() {
		invalidated = true;
	}

	private BlockPos[] getNetworkableBlocks(BlockPos pos) {
		ArrayList<BlockPos> viableCables = new ArrayList<BlockPos>();
		for (Direction dir : Direction.values()) {
			BlockPos adjacent = pos.relative(dir);
			if (cableType.canNetworkWith(level, pos.relative(dir)))
				viableCables.add(adjacent);
		}
		
		return viableCables.toArray(new BlockPos[viableCables.size()]);
	}

	public SecurityServerEntity getSecurityServer() {
		return securityServer;
	}
	
	public DatabaseServerEntity getDatabaseServer() {
		return databaseServer;
	}

	public CableEndpoint[] getEndpoints() {
		return endpoints.values().toArray(new CableEndpoint[endpoints.size()]);
	}

	public CableEndpoint getEndpoint(BlockPos pos) {
		return endpoints.get(pos);
	}

	public interface Visitor {
		boolean visit(BlockPos pos, BlockState state, BlockEntity entity);
	}

	private boolean visit(Visitor visitor) {
		var visited = new HashSet<BlockPos>();
		var sources = new ArrayList<BlockPos>();

		sources.add(this.startingPoint);

		var shouldContinue = true;
		while (shouldContinue && sources.size() > 0) {
			var nextSources = new ArrayList<BlockPos>();

			for (BlockPos source : sources) {
				if (!visited.add(source))
					continue;

				for (BlockPos cable : this.getNetworkableBlocks(source)) {
					var entity = level.getBlockEntity(cable);

					shouldContinue = visitor.visit(cable, level.getBlockState(cable), entity);
					if (!shouldContinue)
						break;

					if (entity instanceof MachineEntity machineEntity) {
						if (machineEntity.actsAsCable()) {
							nextSources.add(cable);
						}
					}
				}

				if (!shouldContinue)
					break;
			}

			sources = nextSources;
		}

		return shouldContinue;
	}
}
