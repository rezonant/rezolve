package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.capabilities.Tunnel;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.storage.NetworkStorageAccessor;
import com.rezolvemc.thunderbolt.databaseServer.DatabaseServerEntity;
import com.rezolvemc.thunderbolt.securityServer.SecurityServerEntity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CableNetwork {
	private static final Logger LOGGER = LogManager.getLogger(Rezolve.ID);
	private static final Capability<Tunnel> TUNNEL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

	private CableNetwork(Level level, BlockPos startingPoint, ThunderboltCable cableType) {
		this.server = level.getServer();

		if (this.server == null)
			throw new RuntimeException("Server is null! Are you building a cable network on the client side??");

		this.levelKey = level.dimension();
		this.startingPoint = startingPoint;
		this.cableType = cableType;
		this.storageAccessor = new NetworkStorageAccessor(this);
	}

	private final MinecraftServer server;
	private final ResourceKey<Level> levelKey;
	private final BlockPos startingPoint;
	private final ThunderboltCable cableType;
	private boolean invalidated = false;
	private SecurityServerEntity securityServer;
	private DatabaseServerEntity databaseServer;
	private NetworkStorageAccessor storageAccessor;
	private final Map<LevelPosition, Endpoint> endpoints = new HashMap<>();

	public NetworkStorageAccessor getStorageAccessor() {
		return storageAccessor;
	}

	/**
	 * Used only for debugging. Should be removed after we've worked out all the cases where we cause cable network
	 * duplication.
	 */
	private static List<CableNetwork> activeCableNetworks = new ArrayList<>();

	private static List<CableNetwork> getActiveCableNetworks() {
		List<CableNetwork> list = new ArrayList<>();
		for (var network : activeCableNetworks) {
			if (network.isInvalidated())
				continue;
			list.add(network);
		}

		activeCableNetworks.clear();
		activeCableNetworks.addAll(list);

		return activeCableNetworks;
	}

	public static CableNetwork boot(Level bootedLevel, BlockPos startingPoint) {
		var network = new CableNetwork(bootedLevel, startingPoint, RezolveRegistry.block(ThunderboltCable.class))
				.boot();

		// TODO This is purely a debugging check. It is incredibly inefficient and should be removed.

		if (!FMLEnvironment.production) {
			network.visit((level, pos, state, entity) -> {
				if (!network.cableType.canConnectTo(level, pos))
					return true;

				for (var otherNetwork : getActiveCableNetworks()) {
					if (otherNetwork == network)
						continue; // shouldn't be possible though

					otherNetwork.visit((otherLevel, otherPos, otherState, otherEntity) -> {
						if (level == otherLevel && pos.equals(otherPos)) {
							LOGGER.warn("No two networks should contain the same cable!");
						}
						return true;
					});
				}

				return true;
			});
		}

		activeCableNetworks.add(network);

		return network;
	}

	private CableNetwork boot() {
		// First, discover all endpoints

		for (var endpoint : computeEndpoints())
			endpoints.put(endpoint.getLevelPosition(), endpoint);

		// Second, have all cables and endpoints adopt the network,
		// now that we have complete endpoint information.
		// We need to wait to do this so that the cables can attach their
		// interface configuration information.

		visit((level, pos, state, entity) -> {
			if (entity instanceof MachineEntity machineEntity)
				machineEntity.adoptNetwork(this);
			return true;
		});

		// Find some special endpoints, like the security server/database server
		// etc.

		for (var endpoint : endpoints.values()) {
			var level = server.getLevel(levelKey);
			var entity = level.getBlockEntity(endpoint.getPosition());

			if (entity instanceof SecurityServerEntity securityServer)
				this.securityServer = securityServer;

			if (entity instanceof DatabaseServerEntity databaseServer)
				this.databaseServer = databaseServer;
		}

		return this;
	}

	private List<Endpoint> computeEndpoints() {
		var endpointPositions = new HashSet<LevelPosition>();

		visit((level, pos, state, entity) -> {
			if (cableType.canInterfaceWith(level, pos))
				endpointPositions.add(new LevelPosition(level.dimension(), pos));
			return true;
		});

		return endpointPositions.stream()
				.map(position -> new Endpoint(server, position.getLevelKey(), position.getPosition()))
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

	private BlockPos[] getNetworkableBlocks(Level level, BlockPos pos) {
		ArrayList<BlockPos> viableCables = new ArrayList<BlockPos>();
		for (Direction dir : Direction.values()) {
			BlockPos adjacent = pos.relative(dir);
			BlockState state = level.getBlockState(adjacent);
			if (cableType.canNetworkWith(level, pos.relative(dir)) || state.getBlock() == Blocks.NETHER_PORTAL)
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

	public Endpoint[] getEndpoints() {
		return endpoints.values().toArray(new Endpoint[endpoints.size()]);
	}

	public Endpoint[] getEndpointsByBlock(Class<? extends Block> blockClass) {
		var list = new ArrayList<Endpoint>();
		for (var endpoint : getEndpoints()) {
			var level = server.getLevel(endpoint.getLevelKey());
			var blockState = level.getBlockState(endpoint.getPosition());
			if (blockClass.isAssignableFrom(blockState.getBlock().getClass()))
				list.add(endpoint);
		}

		return list.toArray(new Endpoint[list.size()]);
	}

	public Endpoint getEndpointByBlock(Class<? extends Block> blockClass) {
		var list = getEndpointsByBlock(blockClass);
		return list.length == 0 ? null : list[0];
	}

	public Endpoint[] getEndpointsByEntity(Class<? extends BlockEntity> block) {
		var list = new ArrayList<Endpoint>();
		for (var endpoint : getEndpoints()) {
			var level = server.getLevel(endpoint.getLevelKey());
			var blockEntity = level.getBlockEntity(endpoint.getPosition());
			if (block != null && block.isAssignableFrom(blockEntity.getClass()))
				list.add(endpoint);
		}

		return list.toArray(new Endpoint[list.size()]);
	}

	public Endpoint getEndpointByEntity(Class<? extends BlockEntity> blockClass) {
		var list = getEndpointsByEntity(blockClass);
		return list.length == 0 ? null : list[0];
	}

	public Endpoint getEndpoint(LevelPosition position) {
		return endpoints.get(position);
	}

	public Endpoint getEndpoint(Level level, BlockPos pos) {
		return getEndpoint(level.dimension(), pos);
	}

	public Endpoint getEndpoint(ResourceKey<Level> level, BlockPos pos) {
		return endpoints.get(new LevelPosition(level, pos));
	}

	public interface Visitor {
		boolean visit(Level level, BlockPos pos, BlockState state, BlockEntity entity);
	}

	private boolean visit(Visitor visitor) {
		var visited = new HashSet<LevelPosition>();
		var sources = new ArrayList<LevelPosition>();

		sources.add(new LevelPosition(levelKey, this.startingPoint));

		var shouldContinue = true;
		while (shouldContinue && sources.size() > 0) {
			var nextSources = new ArrayList<LevelPosition>();

			for (var levelPos : sources) {
				var source = levelPos.getPosition();
				var levelKey = levelPos.getLevelKey();
				var level = server.getLevel(levelKey);

				if (!visited.add(levelPos))
					continue;

				for (BlockPos cable : this.getNetworkableBlocks(level, source)) {
					var blockState = level.getBlockState(cable);
					var entity = level.getBlockEntity(cable);

					shouldContinue = visitor.visit(level, cable, level.getBlockState(cable), entity);
					if (!shouldContinue)
						break;

					// Handle tunnel blocks. Examples would be tesseracts, compact machines, and other interdimensional bridges

					if (entity != null) {
						var tunnelCap = entity.getCapability(TUNNEL_CAPABILITY).orElse(null);
						if (tunnelCap != null) {
							nextSources.add(new LevelPosition(tunnelCap.getDestinationLevel(), tunnelCap.getDestinationPosition()));
						}
					}

					// Special support for Nether portals

					if (blockState.getBlock() == Blocks.NETHER_PORTAL) {
						// First, all nether portal blocks act as cables.
						nextSources.add(new LevelPosition(levelKey, cable));

						// Next, we're going to need to add all of the portal blocks on the _other_ side of this portal
						// to our search list, assuming they have not already been added.

						var otherDimensionKey = levelKey == Level.OVERWORLD ? Level.NETHER : Level.OVERWORLD;
						var otherLevel = server.getLevel(otherDimensionKey);
						var frame = otherLevel.getPortalForcer()
								.findPortalAround(
										cable,
										otherDimensionKey == Level.NETHER,
										otherLevel.getWorldBorder()
								)
								.orElse(null)
								;
						if (frame != null) {
							for (int x = 0, maxX = frame.axis1Size; x < maxX; ++x) {
								for (int y = 0, maxY = frame.axis2Size; y < maxY; ++y) {
									var portalPos = new LevelPosition(otherLevel.dimension(), frame.minCorner.offset(x, y, 0));
									if (!visited.contains(portalPos))
										nextSources.add(portalPos);
								}
							}
						}
					}

					// Simple path: is this a cable?

					if (entity instanceof MachineEntity machineEntity) {
						if (machineEntity.actsAsCable()) {
							nextSources.add(new LevelPosition(levelKey, cable));
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

	public static class Endpoint extends LevelPosition {
		public Endpoint(MinecraftServer server, ResourceKey<Level> level, BlockPos pos) {
			super(level, pos);

			this.levelPosition = new LevelPosition(level, pos);
			this.server = server;
		}

		private final LevelPosition levelPosition;
		private final MinecraftServer server;

		public Level getLevel() {
			return server.getLevel(getLevelKey());
		}

		public BlockEntity getBlockEntity() {
			return getLevel().getBlockEntity(getPosition());
		}

		public <T extends BlockEntity> T getBlockEntity(Class<T> klass) {
			var entity = getBlockEntity();
			if (entity != null && klass.isAssignableFrom(entity.getClass())) {
				return (T)entity;
			}

			return null;
		}

		public Item getBlockItem() {
			return getBlock().asItem();
		}

		public Block getBlock() {
			return getBlockState().getBlock();
		}

		public BlockState getBlockState() {
			return getLevel().getBlockState(getPosition());
		}

		private Map<BlockPos, BlockConfiguration> interfaces = new HashMap<>();

		public BlockConfiguration[] getInterfaces() {
			return interfaces.values().toArray(new BlockConfiguration[interfaces.size()]);
		}

		public void addInterface(BlockPos pos, BlockConfiguration configuration) {
			interfaces.put(pos, configuration);
		}

		public boolean is(LevelPosition position) {
			return Objects.equals(getLevelPosition(), position);
		}

		public boolean is(BlockEntity entity) {
			return Objects.equals(getBlockEntity(), entity);
		}

		public boolean is(Level level) {
			return Objects.equals(getLevel(), level);
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj; // revert to reference equality
		}

		public LevelPosition getLevelPosition() {
			return levelPosition;
		}
	}

	@SubscribeEvent
	public static void serverIsStopping(ServerStoppingEvent event) {
		activeCableNetworks.clear();
	}
}
