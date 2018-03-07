package com.astronautlabs.mc.rezolve.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;

public abstract class ContainerBase<T extends TileEntity> extends Container {

	public ContainerBase(T entity) {
		super();
		
		this.entity = entity;
		this.inventory = (IInventory)entity;
		
	}
	
	protected T entity;
	protected IInventory inventory;
	
	public T getEntity() {
		return this.entity;
	}

	private HashMap<String, Slot> namedSlots = new HashMap<String, Slot>();

	public void addNamedSlot(String name, Slot slot) {
		this.namedSlots.put(name, slot);
		this.addSlotToContainer(slot);
	}

	public Slot getNamedSlot(String name) {
		return this.namedSlots.get(name);
	}

	public void addPlayerSlots(IInventory playerInv, int playerInvOffsetX, int playerInvOffsetY, int invSlotSize) {
		this.addPlayerSlots(playerInv, playerInvOffsetX, playerInvOffsetY, invSlotSize, 47, 189);
	}

	public void addPlayerSlots(IInventory playerInv, int playerInvOffsetX, int playerInvOffsetY, int invSlotSize, int playerHotbarOffsetX, int playerHotbarOffsetY) {
		// Player Inventory, slots 9-35
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x)
				this.addSlotToContainer(new Slot(playerInv, 9 + x + y * 9, playerInvOffsetX + x * invSlotSize, playerInvOffsetY + y * invSlotSize));
		}

		// Player Hotbar, slots 0-8
		for (int x = 0; x < 9; ++x)
			this.addSlotToContainer(new Slot(playerInv, x, playerHotbarOffsetX + x * 18, playerHotbarOffsetY));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		// TODO Auto-generated method stub
		if (this.entity instanceof IInventory)
			return ((IInventory)this.entity).isUseableByPlayer(player);
		
		return true;
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
	    ItemStack previous = null;
	    Slot slot = (Slot) this.inventorySlots.get(fromSlot);

	    if (slot != null && slot.getHasStack()) {
	        ItemStack current = slot.getStack();
	        previous = current.copy();

	        if (fromSlot < 9) {
	            // From TE Inventory to Player Inventory
	            if (!this.mergeItemStack(current, 9, 45, true))
	                return null;
	        } else {
	            // From Player Inventory to TE Inventory
	            if (!this.mergeItemStack(current, 0, 9, false))
	                return null;
	        }

	        if (current.stackSize == 0)
	            slot.putStack((ItemStack) null);
	        else
	            slot.onSlotChanged();

	        if (current.stackSize == previous.stackSize)
	            return null;
	        slot.onPickupFromSlot(playerIn, current);
	    }
	    return previous;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (this.entity instanceof IInventory)
			((IInventory)this.entity).closeInventory(playerIn);
	}
}