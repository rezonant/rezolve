package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;

public class DiskManipulatorBlock extends Machine {

	public DiskManipulatorBlock() {
		super("block_disk_manipulator");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return DiskManipulatorEntity.class;
	}
}
