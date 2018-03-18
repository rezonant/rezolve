package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;

public class DiskManipulatorEntity extends MachineEntity {
	public DiskManipulatorEntity() {
		super("disk_manipulator_tile_entity");
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public Class<? extends MachineGui> getGuiClass() {
		return DiskManipulatorGui.class;
	}
}
