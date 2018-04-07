package com.astronautlabs.mc.rezolve.storage.machines.storageMonitor;

import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;

public class StorageMonitorBlock extends Machine {

	public StorageMonitorBlock() {
		super("block_storage_monitor");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return StorageMonitorEntity.class;
	}
}
