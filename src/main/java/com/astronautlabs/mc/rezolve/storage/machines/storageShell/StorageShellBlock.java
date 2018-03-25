package com.astronautlabs.mc.rezolve.storage.machines.storageShell;

import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;

public class StorageShellBlock extends Machine {

	public StorageShellBlock() {
		super("block_storage_shell");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return StorageShellEntity.class;
	}
}
