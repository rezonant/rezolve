package com.astronautlabs.mc.rezolve.storage;

import com.astronautlabs.mc.rezolve.network.cable.CableNetwork;
import net.minecraft.tileentity.TileEntity;

public class NetworkStorageAccessor extends MultiplexedStorageAccessor implements CableNetwork.IListener {
	public NetworkStorageAccessor(CableNetwork network) {
		this.network = network;
		this.network.addListener(this);
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
			TileEntity entity = this.network.getWorld().getTileEntity(endpoint.getPosition());

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

	@Override
	public void onNetworkUpdated(CableNetwork network) {
		this.loadAccessors();
	}

	@Override
	public void onNetworkInvalidated(CableNetwork network) {
		this.accessors.clear();
	}
}
