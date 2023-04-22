package com.rezolvemc.storage.machines.diskBay;

import com.rezolvemc.common.inventory.ValidatedSlot;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.storage.DiskItem;
import com.rezolvemc.storage.IStorageAccessor;
import com.rezolvemc.storage.IStorageTileEntity;
import com.rezolvemc.storage.MultiplexedStorageAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;

public class DiskBayEntity extends MachineEntity implements IStorageTileEntity {
	public DiskBayEntity(BlockPos pos, BlockState state) {
		super(RezolveRegistry.blockEntityType(DiskBayEntity.class), pos, state);
		this.updateInterval = 5;
		this.maxEnergyStored = 50000;
		this.multiplexedAccessor = new MultiplexedStorageAccessor(new ArrayList<>());

		for (int i = 0, max = 27; i < max; ++i)
			addSlot(new ValidatedSlot(this, i, 0, 0, stack -> isValidDisk(stack)));
	}

	public static boolean isValidDisk(ItemStack stack) {
		return (
				stack == null
						|| stack.getCount() == 0
						|| (stack.getCount() == 1 && stack.getItem() instanceof DiskItem)
		);
	}

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.disk_bay");
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
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			ItemStack stack = this.getStackInSlot(i);
			boolean isValid = stack != null && stack.getCount() > 0 && DiskAccessor.accepts(stack);

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
				this.setItem(disk.getKey(), disk.getValue().getItemStack());
				disk.getValue().acknowledgeChanges();
			}
		}

		this.savingDisks = false;
	}

	private boolean savingDisks = false;

	@Override
	protected void onSlotChanged(Slot slot) {
		super.onSlotChanged(slot);
		if (!this.savingDisks)
			this.createAccessor(slot.index);
	}

	@Override
	public void updatePeriodically() {
		super.updatePeriodically();

		// Periodically ensure we have an accurate view of all of our accessors

		this.syncAccessors();

		// Save the disk contents back to their items periodically

		this.saveDisks();
	}
}
