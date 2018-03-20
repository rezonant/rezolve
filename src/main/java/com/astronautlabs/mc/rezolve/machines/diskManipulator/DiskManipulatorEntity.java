package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import com.astronautlabs.mc.rezolve.machines.diskBay.DiskAccessor;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DiskManipulatorEntity extends MachineEntity implements IItemStorage {
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

	ItemStack loadedDisk = null;
	DiskAccessor loadedDiskAccessor = null;

	@Override
	protected void updatePeriodicallyOnClient() {
		// Client-side
		super.updatePeriodicallyOnClient();
	}

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
		} else if (loadedDisk == null && diskInSlot != null) {
			needsLoad = true;
		} else if (loadedDisk != null && diskInSlot != null) {
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

	@Override
	public int getTotalItems() {
		if (this.loadedDiskAccessor == null)
			return 0;

		return this.loadedDiskAccessor.getTotalItems();
	}

	@Override
	public int getTotalStacks() {
		if (this.loadedDiskAccessor == null)
			return 0;

		return this.loadedDiskAccessor.getTotalStacks();
	}

	@Override
	public List<ItemStack> readItems(String query, int offset, int size) {
		if (this.loadedDiskAccessor == null)
			return new ArrayList<>();

		return this.loadedDiskAccessor.readItems(query, offset, size);
	}

	@Override
	public ItemStack giveItemStack(ItemStack toGive, boolean simulate) {
		if (this.loadedDiskAccessor == null)
			return toGive;

		return this.loadedDiskAccessor.giveItemStack(toGive, simulate);
	}

	@Override
	public ItemStack takeItemStack(ItemStack toTake, boolean simulate) {
		if (this.loadedDiskAccessor == null)
			return ItemStackUtil.getEmptyStack(toTake);

		return this.loadedDiskAccessor.takeItemStack(toTake, simulate);
	}
}
