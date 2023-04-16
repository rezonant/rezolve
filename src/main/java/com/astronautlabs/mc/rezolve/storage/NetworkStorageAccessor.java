package com.astronautlabs.mc.rezolve.storage;

import com.astronautlabs.mc.rezolve.thunderbolt.cable.CableNetwork;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NetworkStorageAccessor extends MultiplexedStorageAccessor {
	public NetworkStorageAccessor(CableNetwork network) {
		this.network = network;
		// this.network.addListener(this); // TODO: probably _has_ to be BlockEntity based
	}

	private CableNetwork network;


	@Override
	protected void onQuery() {
		super.onQuery();

		this.loadAccessors();

	}

	private void loadAccessors() {
		this.accessors.clear();
		for (CableNetwork.Endpoint endpoint : this.network.getEndpoints()) {
			BlockEntity entity = endpoint.getBlockEntity();

			if (entity instanceof IStorageTileEntity) {
				IStorageTileEntity storageTileEntity = (IStorageTileEntity)entity;
				IStorageAccessor accessor =  storageTileEntity.getStorageAccessor();

				if (accessor != this)
					this.accessors.add(accessor);
			}
		}
	}

	@Override
	public void clear() {
		// no.
	}

	// TODO: updates over time
//	@Override
//	public void onNetworkUpdated(CableNetwork network) {
//		this.loadAccessors();
//	}
//
//	@Override
//	public void onNetworkInvalidated(CableNetwork network) {
//		this.accessors.clear();
//	}
}
