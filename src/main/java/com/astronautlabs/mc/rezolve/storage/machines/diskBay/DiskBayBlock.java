package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;

public class DiskBayBlock extends Machine {

	public DiskBayBlock() {
		super("block_disk_bay");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return DiskBayEntity.class;
	}
}
