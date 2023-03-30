package com.astronautlabs.mc.rezolve.common.inventory;

import java.util.Collection;
import java.util.Hashtable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class VirtualInventory implements Container {
	protected ContainerItemHandler handler = new ContainerItemHandler(this);

	public IItemHandler getHandler() {
		return this.handler;
	}

	Hashtable<Integer, ItemStack> slots = new Hashtable<Integer, ItemStack>();
	
	public Collection<ItemStack> getStacks() {
		return this.slots.values();
	}

	@Override
	public int getContainerSize() {
		return 99;
	}

	@Override
	public boolean isEmpty() {
		for (var pair : slots.entrySet()) {
			if (pair.getValue() != null && !pair.getValue().isEmpty())
				return false;
		}

		return true;
	}

	@Override
	public ItemStack getItem(int pSlot) {
		ItemStack stack = this.slots.get(pSlot);
		return stack == null ? ItemStack.EMPTY : stack.copy();
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		ItemStack stack = slots.get(pSlot);
		if (stack == null)
			return ItemStack.EMPTY.copy();

		return stack.split(pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		ItemStack stack = this.slots.get(pSlot);
		this.slots.remove(pSlot);
		return stack;
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		this.slots.put(pSlot, pStack.copy());
	}

	@Override
	public void setChanged() {

	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return false;
	}

	@Override
	public void clearContent() {
		this.slots.clear();
	}
}
