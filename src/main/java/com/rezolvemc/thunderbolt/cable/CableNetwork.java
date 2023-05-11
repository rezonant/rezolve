package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.LevelPosition;
import com.rezolvemc.common.capabilities.EnergyStack;
import com.rezolvemc.common.capabilities.Tunnel;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.util.RezolveCapHelper;
import com.rezolvemc.storage.NetworkStorageAccessor;
import com.rezolvemc.thunderbolt.databaseServer.DatabaseServerEntity;
import com.rezolvemc.thunderbolt.securityServer.SecurityServerEntity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CableNetwork {
	private static final Logger LOGGER = LogManager.getLogger(Rezolve.ID);
	public static final Capability<Tunnel> TUNNEL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

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
			var entity = level.getBlockEntity(endpoint.blockPos);

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
				.map(position -> new Endpoint(server, position.levelKey, position.blockPos))
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
			var level = server.getLevel(endpoint.levelKey);
			var blockState = level.getBlockState(endpoint.blockPos);
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
			var level = server.getLevel(endpoint.levelKey);
			var blockEntity = level.getBlockEntity(endpoint.blockPos);
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
				var source = levelPos.blockPos;
				var levelKey = levelPos.levelKey;
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
							nextSources.addAll(tunnelCap.getProxyDestinations());
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

	private boolean lockedForTransfer = false;

	private void lockForTransfer(Runnable runnable) {
		lockForTransfer(null, () -> {
			runnable.run();
			return null;
		});
	}

	private <T> T lockForTransfer(T defaultValue, Supplier<T> runnable) {
		if (lockedForTransfer)
			return defaultValue;

		lockedForTransfer = true;
		try {
			return runnable.get();
		} finally {
			lockedForTransfer = false;
		}
	}

	/**
	 * Perform an item transfer from the given start point to whatever end point will accept it.
	 * @param transmit
	 * @param face
	 * @param inletBlock
	 */
	public void transferItem(
		TransmitConfiguration transmit,
		FaceConfiguration face,
		LevelPosition inletBlock
	) {
		var inletLevel = server.getLevel(inletBlock.levelKey);
		var amount = 64;
		var entity = inletLevel.getBlockEntity(inletBlock.blockPos);

		var itemHandler = RezolveCapHelper.getItemHandler(entity, face.getDirection());
		if (itemHandler != null) {
			for (int sourceSlot = 0, max = itemHandler.getSlots(); sourceSlot < max; ++sourceSlot) {
				var potentialStack = itemHandler.extractItem(sourceSlot, amount, true);
				if (potentialStack != null && !potentialStack.isEmpty()) {
					int finalSourceSlot = sourceSlot;
					var success = receiveItem(
						potentialStack,
						desiredAmount -> itemHandler.extractItem(finalSourceSlot, desiredAmount, false),
						inletBlock, false
					);

					if (success)
						return;
				}
			}
		}
	}

	public boolean pushItem(
		ItemStack itemsToTransfer,
		Function<Integer, ItemStack> extractor,
		LevelPosition sourceEndpoint,
		boolean simulate
	) {
		return lockForTransfer(false, () -> receiveItem(itemsToTransfer, extractor, sourceEndpoint, simulate));
	}

	private boolean receiveItem(
		ItemStack itemsToTransfer,
		Function<Integer, ItemStack> extractor,
		LevelPosition sourceEndpoint,
		boolean simulate
	) {
		itemsToTransfer = itemsToTransfer.copy();
		for (var dest : getEndpoints()) {
			if (dest.is(sourceEndpoint))
				continue;

			var destEntity = dest.getBlockEntity();

			for (var blockInterface : dest.getInterfaces()) {
				for (var destFace : blockInterface.getFaces()) {
					var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.ITEMS);
					if (itemInterface.getMode().canPush()) {
						var destHandler = RezolveCapHelper.getItemHandler(destEntity, destFace.getDirection());
						if (destHandler == null)
							continue;

						for (int destinationSlot = 0, destSlotCount = destHandler.getSlots(); destinationSlot < destSlotCount; ++destinationSlot) {
							var remainder = destHandler.insertItem(destinationSlot, itemsToTransfer, true);
							if (remainder == null)
								remainder = ItemStack.EMPTY;

							var acceptedAmount = itemsToTransfer.getCount() - remainder.getCount();

							var item = extractor.apply(acceptedAmount);
							destHandler.insertItem(destinationSlot, item, simulate);

							itemsToTransfer = remainder;

							if (itemsToTransfer.isEmpty())
								return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Transfer fluid from the given source block into a destination within the network.
	 *
	 * @param transmitConfig
	 * @param face
	 * @param levelPos
	 */
	public void transferFluid(
		TransmitConfiguration transmitConfig,
		FaceConfiguration face,
		LevelPosition levelPos
	) {
		var amount = 1000;
		var inletLevel = server.getLevel(levelPos.levelKey);
		var entity = inletLevel.getBlockEntity(levelPos.blockPos);

		var sourceHandler = entity.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getDirection()).orElse(null);
		if (sourceHandler == null)
			return;

		var potentialStack = sourceHandler.drain(amount, IFluidHandler.FluidAction.SIMULATE);
		if (potentialStack != null && !potentialStack.isEmpty()) {
			receiveFluid(
				potentialStack,
				desiredAmount -> sourceHandler.drain(desiredAmount, IFluidHandler.FluidAction.EXECUTE),
				levelPos, false
			);
		}
	}

	public boolean pushFluid(
		FluidStack fluidToTransfer,
		Function<Integer, FluidStack> extractor,
		LevelPosition sourceEndpoint,
		boolean simulate
	) {
		return lockForTransfer(false, () -> receiveFluid(fluidToTransfer, extractor, sourceEndpoint, simulate));
	}

	/**
	 * Push fluid into the network from the given source endpoint
	 *
	 * @param fluidToTransfer
	 * @param extractor
	 * @param sourceEndpoint
	 * @param simulate
	 * @return
	 */
	private boolean receiveFluid(
		FluidStack fluidToTransfer,
		Function<Integer, FluidStack> extractor,
		LevelPosition sourceEndpoint,
		boolean simulate
	) {
		fluidToTransfer = fluidToTransfer.copy();

		for (var dest : getEndpoints()) {
			if (dest.is(sourceEndpoint))
				continue;

			var destEntity = dest.getBlockEntity();

			for (var blockInterface : dest.getInterfaces()) {
				for (var destFace : blockInterface.getFaces()) {
					var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.FLUIDS);
					if (itemInterface.getMode().canPush()) {
						var destHandler = RezolveCapHelper.getFluidHandler(destEntity, destFace.getDirection());
						if (destHandler == null)
							continue;

						var receivableAmount = destHandler.fill(fluidToTransfer, IFluidHandler.FluidAction.SIMULATE);
						if (receivableAmount <= 0)
							continue;

						// This will work

						var actualStack = extractor.apply(receivableAmount);
						var filled = destHandler.fill(actualStack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);

						fluidToTransfer.setAmount(fluidToTransfer.getAmount() - filled);

						if (fluidToTransfer.isEmpty())
							return true;
					}
				}
			}
		}

		return false;
	}

	public void transferEnergy(
		TransmitConfiguration transmitTypeConfig,
		FaceConfiguration face,
		LevelPosition inletPos
	) {
		var amount = 1000000;
		var inletLevel = server.getLevel(inletPos.levelKey);
		var entity = inletLevel.getBlockEntity(inletPos.blockPos);
		var sourceHandler = entity.getCapability(ForgeCapabilities.ENERGY, face.getDirection()).orElse(null);
		if (sourceHandler == null)
			return;

		var availableAmount = sourceHandler.extractEnergy(amount, true);
		if (availableAmount > 0) {
			receiveEnergy(
				new EnergyStack(availableAmount),
				desiredAmount -> EnergyStack.of(sourceHandler.extractEnergy(desiredAmount, false)),
				inletPos,
				false
			);
		}
	}

	public boolean pushEnergy(
		EnergyStack potentialEnergyStack,
		Function<Integer, EnergyStack> extractor,
		LevelPosition sourceEndpoint,
		boolean simulate
	) {
		return lockForTransfer(false, () -> receiveEnergy(potentialEnergyStack, extractor, sourceEndpoint, simulate));
	}

	private boolean receiveEnergy(
		EnergyStack potentialEnergyStack,
		Function<Integer, EnergyStack> extractor,
		LevelPosition sourceEndpoint,
		boolean simulate
	) {

		potentialEnergyStack = potentialEnergyStack.copy();

		for (var dest : getEndpoints()) {
			if (dest.is(sourceEndpoint))
				continue;

			var destEntity = dest.getBlockEntity();

			for (var blockInterface : dest.getInterfaces()) {
				for (var destFace : blockInterface.getFaces()) {
					var itemInterface = destFace.getTransmissionConfiguration(TransmissionType.ENERGY);
					if (itemInterface.getMode().canPush()) {
						var destHandler = RezolveCapHelper.getEnergyStorage(destEntity, destFace.getDirection());
						if (destHandler == null)
							continue;

						var receivableAmount = destHandler.receiveEnergy(potentialEnergyStack.getAmount(), true);
						if (receivableAmount <= 0)
							continue;

						var actualTransferred = extractor.apply(receivableAmount);
						destHandler.receiveEnergy(actualTransferred.getAmount(), simulate);
						potentialEnergyStack.split(actualTransferred.getAmount());

						if (potentialEnergyStack.isEmpty())
							return true;
					}
				}
			}
		}

		return false;
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
			return server.getLevel(levelKey);
		}

		public BlockEntity getBlockEntity() {
			return getLevel().getBlockEntity(blockPos);
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
			return getLevel().getBlockState(blockPos);
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
