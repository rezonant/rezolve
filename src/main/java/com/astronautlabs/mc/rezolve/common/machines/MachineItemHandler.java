package com.astronautlabs.mc.rezolve.common.machines;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MachineItemHandler implements IItemHandler {
	public MachineItemHandler(MachineEntity entity) {
		this.entity = entity;
	}
	
	private MachineEntity entity;

	@Override
	public int getSlots() {
		return this.entity.getSlotCount();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.entity.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return this.entity.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return this.entity.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slotId) {
		var slot = this.entity.getSlot(slotId);
		if (slot == null)
			return 0;

		return slot.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slotId, @NotNull ItemStack stack) {
		var slot = this.entity.getSlot(slotId);

		if (slot == null)
			return false;

		return slot.mayPlace(stack);
	}
}
