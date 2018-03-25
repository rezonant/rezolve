package com.astronautlabs.mc.rezolve.network.cable;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.astronautlabs.mc.rezolve.network.machines.databaseServer.DatabaseServerEntity;
import com.astronautlabs.mc.rezolve.network.machines.securityServer.SecurityServerEntity;

import com.astronautlabs.mc.rezolve.storage.NetworkStorageAccessor;
import com.astronautlabs.mc.rezolve.util.DirectionUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CableNetwork {
	private static HashMap<String, SoftReference<CableNetwork>> cableNetworkCache = new HashMap<>();

	public static CableNetwork networkAt(World world, BlockPos pos, CableBlock cableType) {
		return networkAt(world, pos, cableType, true);
	}

	public World getWorld() {
		return this.world;
	}

	private boolean invalidated = false;

	public static CableNetwork networkAt(World world, BlockPos pos, CableBlock cableType, boolean create) {

		if (world.isRemote)
			return null;

		String blockKey = keyForBlockPos(pos);
		CableNetwork network;

		if (cableNetworkCache.containsKey(blockKey)) {
			SoftReference<CableNetwork> networkRef = cableNetworkCache.get(blockKey);
			network = networkRef.get();

			if (network != null && !network.invalidated)
				return network;
		}

		if (!create)
			return null;

		network = new CableNetwork(world, pos, cableType);

		IBlockState startingBlockState = world.getBlockState(pos);

		if (!network.isCableCompatible(startingBlockState))
			return null;

		network.analyzeNetwork();

		return network;
	}

	/**
	 * Construct a new CableNetwork.
	 * @param world
	 * @param startingPoint
	 * @param cableType
	 */
	private CableNetwork(World world, BlockPos startingPoint, CableBlock cableType) {
		this.world = world;
		this.startingPoint = startingPoint;
		this.cableType = cableType;
	}

	/**
	 * The world the network is in
	 */
	private World world;
	private BlockPos startingPoint;
	private CableBlock cableType;
	private HashMap<String, Endpoint> endpoints;
	private HashMap<String, BlockPos> cables;

	/**
	 * Represents an endpoint within the network
	 */
	public static class Endpoint {
		public Endpoint(BlockPos pos, EnumFacing facing) {
			this.position = pos;
			this.connectedSides.add(facing);
		}

		private BlockPos position;
		private List<EnumFacing> connectedSides = new ArrayList<>();

		public void addConnectedSide(EnumFacing face) {
			if (this.connectedSides.contains(face))
				return;

			this.connectedSides.add(face);
		}

		public BlockPos getPosition() {
			return this.position;
		}

		public EnumFacing[] getFacing() {
			return this.connectedSides.toArray(new EnumFacing[this.connectedSides.size()]);
		}
	}

	public static String keyForBlockPos(BlockPos pos) {
		return pos.toString();
	}

	/**
	 * Call this when the network is modified. Calling it will
	 * drop the network from the block->network cache system.
	 *
	 * Each change to the network always yields zero or more new CableNetwork objects.
	 * A CableNetwork only lasts as long as the exact physical space
	 * scanning is still done in a lazy-loaded manner.
	 */
	public void invalidate(World world) {
		this.invalidated = true;
		this.notifyListenersOfInvalidation(world);

		this.invalidateEndpoints();
		this.removeFromCache();
	}

	private void removeFromCache() {
		if (this.cables != null) {
			for (String cableKey : this.cables.keySet())
				cableNetworkCache.remove(cableKey);
		}

		if (this.endpoints != null) {
			for (String endpointKey : this.endpoints.keySet())
				cableNetworkCache.remove(endpointKey);
		}
	}

	/**
	 * The next time we need to know what endpoints are connected to this network,
	 * rewalk the network.
	 */
	public void invalidateEndpoints() {
		this.endpoints = null;
		this.cables = null;
	}

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

	public interface IListener {
		void onNetworkUpdated(CableNetwork network);
		void onNetworkInvalidated(CableNetwork network);
	}

	private NetworkStorageAccessor accessor;
	public NetworkStorageAccessor getNetworkStorage() {
		if (this.accessor == null) {
			this.accessor = new NetworkStorageAccessor(this);
		}
		return this.accessor;
	}

	ArrayList<WeakReference<IListener>> registeredListeners = new ArrayList<>();

	public void addListener(IListener listener) {
		this.registeredListeners.add(new WeakReference<>(listener));
	}

	public void removeListener(IListener listener) {

		ArrayList<WeakReference<IListener>> remainingListeners = new ArrayList<>();

		for (WeakReference<IListener> weakRef : this.registeredListeners) {
			IListener existingListener = weakRef.get();

			if (existingListener == null)
				continue;

			if (existingListener == listener)
				continue;

			remainingListeners.add(weakRef);
		}

		this.registeredListeners = remainingListeners;
	}

	public void notifyListenersOfUpdate(World world) {
		ArrayList<WeakReference<IListener>> toRemove = new ArrayList<>();

		for (WeakReference<IListener> listenerRef : this.registeredListeners) {
			IListener listener = listenerRef.get();

			if (listener == null) {
				toRemove.add(listenerRef);
				continue;
			}

			listener.onNetworkUpdated(this);
		}

		this.registeredListeners.removeAll(toRemove);

		for (Endpoint endpoint : this.endpoints.values()) {
			TileEntity entity = world.getTileEntity(endpoint.getPosition());

			if (entity instanceof IListener) {
				((IListener)entity).onNetworkUpdated(this);
			}
		}
	}

	public void notifyListenersOfInvalidation(World world) {
		ArrayList<WeakReference<IListener>> toRemove = new ArrayList<>();

		for (WeakReference<IListener> listenerRef : this.registeredListeners) {
			IListener listener = listenerRef.get();

			if (listener == null) {
				toRemove.add(listenerRef);
				continue;
			}

			listener.onNetworkInvalidated(this);
		}

		this.registeredListeners.removeAll(toRemove);

		for (Endpoint endpoint : this.endpoints.values()) {
			TileEntity entity = world.getTileEntity(endpoint.getPosition());

			if (entity instanceof IListener) {
				((IListener)entity).onNetworkInvalidated(this);
			}
		}
	}
	
	public EnumFacing[] getNeighboringCablesAndEndpoints(IBlockAccess world, BlockPos pos, BlockPos fromBlock) {
		IBlockState thisBlock = world.getBlockState(pos);
		
		ArrayList<EnumFacing> viableCables = new ArrayList<>();

		for (EnumFacing dir : this.getSurroundingPositions()) {
			BlockPos adj = pos.offset(dir);

			if (!this.world.isAreaLoaded(adj, 1))
				continue;

			//if (fromBlock != null && adj.equals(fromBlock))
			//	continue;
			
			if (!this.cableType.canConnectTo(world, pos, thisBlock, dir, adj))
				continue;

			viableCables.add(dir);
		}
		
		return viableCables.toArray(new EnumFacing[viableCables.size()]);
	}
	
	public SecurityServerEntity getSecurityServer() {
		return this.getSecurityServer(this.getEndpointBlocks());
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
		return this.getDatabaseServer(this.getEndpointBlocks());
	}

	public DatabaseServerEntity getDatabaseServer(BlockPos[] endpoints) {
		for (BlockPos pos : endpoints) {
			TileEntity entity = this.world.getTileEntity(pos);

			if (entity instanceof DatabaseServerEntity)
				return (DatabaseServerEntity)entity;
		}

		return null;
	}

	public static final int MAX_VISIT_LIMIT = 15000;

	public Endpoint[] getEndpoints() {
		if (this.endpoints == null)
			this.analyzeNetwork();

		if (this.endpoints == null)
			return new Endpoint[0];

		return this.endpoints.values().toArray(new Endpoint[this.endpoints.size()]);
	}

	public BlockPos[] getCables() {
		if (this.cables == null)
			this.analyzeNetwork();

		if (this.cables == null)
			return new BlockPos[0];

		return this.cables.values().toArray(new BlockPos[this.cables.size()]);
	}
	/**
	 * @return
	 */
	public BlockPos[] getEndpointBlocks() {
		if (this.endpoints == null)
			this.analyzeNetwork();

		if (this.endpoints == null)
			return new BlockPos[0];

		List<BlockPos> legacy = new ArrayList<BlockPos>();
		for (Endpoint endpoint : this.endpoints.values())
			legacy.add(endpoint.getPosition());

		return legacy.toArray(new BlockPos[legacy.size()]);
	}

	/**
	 * Lazy version of endpointChanged(), we look up the block state and entity for you.
	 * @param world
	 * @param cablePos
	 * @param pos
	 */
	public void endpointChanged(World world, BlockPos cablePos, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		TileEntity entity = world.getTileEntity(pos);

		this.endpointChanged(world, cablePos, pos, blockState, entity);
	}

	/**
	 * Notify the cable network that the given "endpoint" location has changed. "Endpoints"
	 * are the neighbors of a cable block within the world. Cables should venture to notify
	 * the network whenever their neighbors change so that the network information can be kept up
	 * to date.
	 *
	 * Note that if the given neighbor has itself become a new cable, this method will cause this network
	 * to be invalidated and replaced.
	 * @param world
	 * @param cablePos
	 * @param pos
	 * @param state
	 * @param entity
	 */
	public void endpointChanged(World world, BlockPos cablePos, BlockPos pos, IBlockState state, TileEntity entity) {
		String blockKey = keyForBlockPos(pos);
		boolean wasCable = this.cables.containsKey(blockKey);
		boolean isCable = this.isCableCompatible(state);

		/*
		 * First, if the affected location used to be a cable and it is no longer (or vice versa),
		 * the whole network is invalid and needs to be recomputed from scratch.
		 */
		if (wasCable != isCable) {
			System.out.println("New cable detected in endpointChanged(): Invalidating network (source: "+blockKey+")");
			this.invalidate(world);
			return;
		}

		EnumFacing face = DirectionUtil.closestFace(cablePos, pos);
		boolean isEndpoint = this.isCompatibleEndpoint(cablePos, world.getBlockState(cablePos), face, pos);
		boolean wasEndpoint = this.endpoints.containsKey(blockKey);
		boolean changed = false;

		if (isEndpoint && !wasEndpoint) {
			// Added an endpoint
			System.out.println("Added an endpoint at "+blockKey);
			this.endpoints.put(blockKey, new Endpoint(pos, face));
			changed = true;
		} else if (!isEndpoint && wasEndpoint) {
			// Removed an endpoint
			System.out.println("Removed endpoint at "+blockKey);
			this.endpoints.remove(blockKey);
			changed = true;
		}

		if (changed)
			this.notifyListenersOfUpdate(world);
	}

	public void cableRemoved(World world, BlockPos cablePos) {
		this.invalidate(world);
	}

	public boolean isCompatibleEndpoint(BlockPos cablePos, IBlockState cableState, EnumFacing face, BlockPos position) {

		return this.cableType.canConnectTo(world, cablePos, cableState, face, position);
	}

	public boolean isCable(IBlockState cableBlockState) {
		return cableBlockState.getBlock() == this.cableType;
	}

	public boolean isCableCompatible(IBlockState blockState) {
		return this.isCable(blockState)
			|| blockState.getBlock() instanceof ICableCompatible;
	}


	private void analyzeNetwork() {
		HashMap<String, Endpoint> endpoints = new HashMap<>();
		ArrayList<BlockPos> visited = new ArrayList<>();
		ArrayList<BlockPos> sourceCablePositions = new ArrayList<>();
		HashMap<String, BlockPos> cables = new HashMap<>();

		BlockPos fromBlock = null;
		
		sourceCablePositions.add(this.startingPoint);
		
		while (true) {
			ArrayList<BlockPos> nextSources = new ArrayList<>();
			
			for (BlockPos sourceCablePos : sourceCablePositions) {
				if (visited.size() > MAX_VISIT_LIMIT) {
					System.err.println("Hit maximum connected cables ("+visited.size()+" cables), skipping remaining cables in network :-(");
					break;
				}

				// Skip this source if we've already visited it

				if (visited.contains(sourceCablePos))
					continue;

				visited.add(sourceCablePos);

				// Add this cable to the cables set.

				String sourceKey = keyForBlockPos(sourceCablePos);
				IBlockState sourceState = world.getBlockState(sourceCablePos);
				cables.put(sourceKey, sourceCablePos);


				// Evaluate the neighbors of this cable to see if there are more cables or
				// endpoints to add to the network.

				for (EnumFacing neighborDirection : this.getNeighboringCablesAndEndpoints(this.world, sourceCablePos, fromBlock)) {
					BlockPos cable = sourceCablePos.offset(neighborDirection);
					String cableKey = keyForBlockPos(cable);

					if (!this.world.isAreaLoaded(cable, 1))
						continue;

					IBlockState cableBlockState = this.world.getBlockState(cable);
					boolean isCable = this.isCable(cableBlockState);
					boolean isCableOrEquivalent = isCable || cableBlockState.getBlock() instanceof ICableCompatible;
					boolean isCompatibleEndpoint = this.isCompatibleEndpoint(sourceCablePos, sourceState, neighborDirection, cable);

					// If this is an endpoint (and not a cable)
					// we will add it to the endpoints set.


					// Continue along to visit the neighbors of this block if it is acting as a cable.

					if (isCableOrEquivalent)
						nextSources.add(cable);

					if (isCompatibleEndpoint && !isCable) {
						if (endpoints.containsKey(cableKey)) {
							Endpoint endpoint = endpoints.get(cableKey);
							endpoint.addConnectedSide(neighborDirection.getOpposite());
						} else {
							endpoints.put(cableKey, new Endpoint(cable, neighborDirection.getOpposite()));
						}
					}
				}
				
				fromBlock = sourceCablePos;
			}
			
			if (nextSources.size() == 0)
				break;
			
			sourceCablePositions = nextSources;
		}

		this.endpoints = endpoints;
		this.cables = cables;

		// Always install to cache. This way, even when only the endpoints are invalidated,
		// we will pick up new cables and install a cached network entry for those cable positions.
		// When the cables get destroyed, that triggers a full invalidation, and this network gets
		// replaced with zero or more networks.
		this.installToCache();

		this.notifyListenersOfUpdate(world);
	}

	private void installToCache() {
		for (BlockPos cablePos : this.cables.values())
			cableNetworkCache.put(keyForBlockPos(cablePos), new SoftReference<>(this));

		for (Endpoint endpoint : this.endpoints.values())
			cableNetworkCache.put(keyForBlockPos(endpoint.getPosition()), new SoftReference<>(this));
	}
}
