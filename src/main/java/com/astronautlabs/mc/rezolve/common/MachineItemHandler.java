package com.astronautlabs.mc.rezolve.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class MachineItemHandler implements IItemHandler {
	public MachineItemHandler(MachineEntity entity) {
		this.entity = entity;
	}
	
	private MachineEntity entity;

	@Override
	public int getSlots() {
		return this.entity.getSlots();
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
}
