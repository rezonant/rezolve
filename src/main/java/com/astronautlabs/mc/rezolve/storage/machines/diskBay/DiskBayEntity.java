package com.astronautlabs.mc.rezolve.storage.machines.diskBay;

import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.MultiplexedStorageAccessor;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class DiskBayEntity extends MachineEntity implements IStorageTileEntity {

	public DiskBayEntity() {
		super("disk_bay_tile_entity");
		this.updateInterval = 5;
		this.maxEnergyStored = 50000;
		this.multiplexedAccessor = new MultiplexedStorageAccessor(new ArrayList<>());
	}

	@Override
	public IStorageAccessor getStorageAccessor() {
		return this.multiplexedAccessor;
	}

	private MultiplexedStorageAccessor multiplexedAccessor = null;
	private HashMap<Integer, DiskAccessor> accessors = new HashMap<>();

	@Override
	public boolean hasView() {
		return false;
	}

	private void syncAccessors() {
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			ItemStack stack = this.getStackInSlot(i);
			boolean isValid = stack != null && stack.stackSize > 0 && DiskAccessor.accepts(stack);

			if (!isValid) {
				// No item here. Cool?
				if (this.accessors.containsKey(i)) {
					this.accessors.remove(i);
					// TODO: notify
				}
				return;
			}

			// The slot is populated with a compatible item.

			if (!this.accessors.containsKey(i))
				this.createAccessor(i);
		}
	}

	private void createAccessor(int index) {
		ItemStack stack = this.getStackInSlot(index);

		if (DiskAccessor.accepts(stack)) {
			if (this.accessors.containsKey(index)) {
				// all good already
			} else {
				DiskAccessor accessor = new DiskAccessor(stack);
				this.accessors.put(index, accessor);
			}

		} else {
			if (this.accessors.containsKey(index))
				this.accessors.remove(index);
		}

		this.multiplexedAccessor.setAccessors(new ArrayList<>(this.accessors.values()));
	}

	private void saveDisks() {
		this.savingDisks = true;
		for (HashMap.Entry<Integer, DiskAccessor> disk : this.accessors.entrySet()) {
			if (disk.getValue().hasChanges()) {
				this.setInventorySlotContents(disk.getKey(), disk.getValue().getItemStack());
				disk.getValue().acknowledgeChanges();
			}
		}

		this.savingDisks = false;
	}

	private boolean savingDisks = false;

	@Override
	public void onSlotChanged(int index, ItemStack stack) {
		super.onSlotChanged(index, stack);

		if (!this.savingDisks)
			this.createAccessor(index);
	}

	@Override
	public void updatePeriodically() {
		super.updatePeriodically();

		// Periodically ensure we have an accurate view of all of our accessors

		this.syncAccessors();

		// Save the disk contents back to their items periodically

		this.saveDisks();
	}

	// thoughts: syncing disks

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
