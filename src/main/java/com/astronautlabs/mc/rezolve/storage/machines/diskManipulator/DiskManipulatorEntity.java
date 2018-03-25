package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.storage.IItemStorage;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskAccessor;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DiskManipulatorEntity extends MachineEntity implements IStorageTileEntity {
	public DiskManipulatorEntity() {
		super("disk_manipulator_tile_entity");
	}

	@Override
	public IStorageAccessor getStorageAccessor() {
		return this.loadedDiskAccessor;
	}

	@Override
	public boolean hasView() {
		return true;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public Class<? extends MachineGui> getGuiClass() {
		return DiskManipulatorGui.class;
	}

	private ItemStack loadedDisk = null;
	private DiskAccessor loadedDiskAccessor = null;

	@Override
	public void updatePeriodically() {
		super.updatePeriodically();

		// Server-side

		ItemStack diskInSlot = this.getStackInSlot(0);
		boolean needsLoad = false;

		if (loadedDisk != null && diskInSlot == null) {
			loadedDisk = null;
			loadedDiskAccessor = null;
			needsLoad = false;

			System.out.println("Ejected disk.");

		} else if (loadedDisk == null && diskInSlot == null) {
			needsLoad = false;
		} else if (loadedDisk == null) {
			needsLoad = true;
		} else {
			needsLoad = !ItemStack.areItemStacksEqual(loadedDisk, diskInSlot);
		}

		if (needsLoad && diskInSlot.getItem() == RezolveMod.DISK_ITEM) {
			System.out.println("Loading disk...");
			loadedDisk = diskInSlot;
			loadedDiskAccessor = new DiskAccessor(diskInSlot);

			System.out.println(" - Used     : "+loadedDiskAccessor.getTotalItems());
			System.out.println(" - Capacity : "+loadedDiskAccessor.getSize());

		} else if (loadedDisk != null) {
			// Sync the disk back
			ItemStack newItem = loadedDiskAccessor.getItemStack();
			this.setInventorySlotContents(0, newItem);
			this.loadedDisk = newItem;
		}
	}
}
