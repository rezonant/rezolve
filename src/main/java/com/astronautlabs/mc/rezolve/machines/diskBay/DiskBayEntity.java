package com.astronautlabs.mc.rezolve.machines.diskBay;

import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import net.minecraft.item.ItemStack;

public class DiskBayEntity extends MachineEntity {

	public DiskBayEntity() {
		super("disk_bay_tile_entity");
		this.updateInterval = 5;
		this.maxEnergyStored = 50000;
	}

	@Override
	public int getSizeInventory() {
		return 27;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public Class<? extends MachineGui> getGuiClass() {
		return DiskBayGui.class;
	}

}
