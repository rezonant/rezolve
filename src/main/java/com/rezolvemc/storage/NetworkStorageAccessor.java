package com.rezolvemc.storage;

import com.rezolvemc.thunderbolt.cable.CableNetwork;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;

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

				if (!(accessor instanceof NetworkStorageAccessor))
					this.accessors.add(accessor);
			} else {
				var itemHandler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);

				if (itemHandler != null) {
					this.accessors.add(new ItemHandlerStorageAccessor(itemHandler));
				} else {
					var set = new HashSet<IItemHandler>();

					for (var dir : Direction.values()) {
						itemHandler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(null);
						if (itemHandler != null && set.add(itemHandler))
							this.accessors.add(new ItemHandlerStorageAccessor(itemHandler));
					}
				}
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
