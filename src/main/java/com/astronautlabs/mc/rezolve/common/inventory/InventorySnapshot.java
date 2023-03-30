package com.astronautlabs.mc.rezolve.common.inventory;

import net.minecraft.world.item.ItemStack;

public class InventorySnapshot {
	public InventorySnapshot(ItemStack[] slots) {
		this.slots = slots;
	}
	
	private ItemStack[] slots;
	
	public ItemStack[] getSlots() {
		return this.slots;
	}
}
