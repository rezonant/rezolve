package com.rezolvemc.storage.machines.diskManipulator;

import org.torchmc.inventory.ValidatedSlot;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.storage.IStorageAccessor;
import com.rezolvemc.storage.IStorageTileEntity;
import com.rezolvemc.storage.machines.diskBay.DiskAccessor;
import com.rezolvemc.storage.machines.diskBay.DiskBayEntity;
import com.rezolvemc.storage.DiskItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DiskManipulatorEntity extends MachineEntity implements IStorageTileEntity {
	public DiskManipulatorEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(DiskManipulatorEntity.class), pPos, pBlockState);
		addSlot(new ValidatedSlot(this, 0, s -> DiskBayEntity.isValidDisk(s)));
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

	public boolean hasDisk() {
		return loadedDiskAccessor != null;
	}

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.disk_manipulator");
	}

	@Override
	protected void onSlotChanged(Slot slot) {
		if (slot.index == 0) {
			updateDisk();
		}
		super.onSlotChanged(slot);
	}

	private void updateDisk() {
		ItemStack diskInSlot = this.getStackInSlot(0);
		boolean needsLoad = false;

		if (loadedDisk != null && diskInSlot.isEmpty()) {
			loadedDisk = null;
			loadedDiskAccessor = null;
			needsLoad = false;

			System.out.println("Ejected disk.");

		} else if (loadedDisk == null && diskInSlot == null) {
			needsLoad = false;
		} else if (loadedDisk == null) {
			needsLoad = true;
		} else {
			needsLoad = !ItemStack.isSameItemSameTags(loadedDisk, diskInSlot);
		}

		if (needsLoad && diskInSlot.getItem() instanceof DiskItem) {
			System.out.println("Loading disk...");
			loadedDisk = diskInSlot;
			loadedDiskAccessor = new DiskAccessor(diskInSlot, disk -> {
				setItem(0, disk);
				loadedDisk = disk;
			});

			System.out.println(" - Used     : "+loadedDiskAccessor.getTotalItems());
			System.out.println(" - Capacity : "+loadedDiskAccessor.getSize());
		}
	}

	@Override
	public void updatePeriodically() {
		super.updatePeriodically();

		// Server-side

	}
}
