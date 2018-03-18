package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.machines.diskManipulator.DiskManipulatorEntity;

public class DiskManipulatorBlock extends Machine {

	public DiskManipulatorBlock() {
		super("block_disk_manipulator");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return DiskManipulatorEntity.class;
	}
}
