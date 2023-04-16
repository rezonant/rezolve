package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import com.astronautlabs.mc.rezolve.common.inventory.ValidatedSlot;
import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskAccessor;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskBayEntity;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DiskManipulatorEntity extends MachineEntity implements IStorageTileEntity {
	public DiskManipulatorEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(DiskManipulatorEntity.class), pPos, pBlockState);
		addSlot(new ValidatedSlot(this, 0, 0, 0, s -> DiskBayEntity.isValidDisk(s)));
	}

	@Override
	public IStorageAccessor getStorageAccessor() {
		return this.loadedDiskAccessor;
	}

	@Override
	public boolean hasView() {
		return true;
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
			needsLoad = !ItemStack.isSame(loadedDisk, diskInSlot);
		}

		if (needsLoad && diskInSlot.getItem() == RezolveRegistry.item(DiskItem.class)) {
			System.out.println("Loading disk...");
			loadedDisk = diskInSlot;
			loadedDiskAccessor = new DiskAccessor(diskInSlot);

			System.out.println(" - Used     : "+loadedDiskAccessor.getTotalItems());
			System.out.println(" - Capacity : "+loadedDiskAccessor.getSize());

		} else if (loadedDisk != null) {
			// Sync the disk back
			ItemStack newItem = loadedDiskAccessor.getItemStack();
			this.setItem(0, newItem);
			this.loadedDisk = newItem;
		}
	}
}
