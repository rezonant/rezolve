package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.storage.IItemStorage;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.gui.StorageViewRecipeRequestMessage;
import com.astronautlabs.mc.rezolve.storage.gui.StorageViewRequestMessage;
import com.astronautlabs.mc.rezolve.storage.gui.StorageViewSession;
import com.astronautlabs.mc.rezolve.storage.gui.StorageViewStateMessage;
import com.astronautlabs.mc.rezolve.storage.machines.storageShell.StorageShellEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private HashMap<String, StorageViewSession> storageViewSessions = new HashMap<>();

	private IStorageTileEntity itemStorage;
	private boolean _isStorageCapabilityTested = false;

	public boolean isStorageCapable() {
		if (this._isStorageCapabilityTested)
			return this.itemStorage != null;

		this._isStorageCapabilityTested = true;
		if (this.entity instanceof IStorageTileEntity) {
			itemStorage = (IStorageTileEntity)this.entity;
		}

		return this.itemStorage != null;
	}

	public IStorageTileEntity getStorageTileEntity() {
		return this.itemStorage;
	}


	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);

		if (this.isStorageCapable()) {
			if (listener instanceof EntityPlayerMP) {
				// Hello player! Let's add you a StorageViewSession
				EntityPlayerMP player = (EntityPlayerMP) listener;

				if (this.itemStorage.hasView())
					this.storageViewSessions.put(player.getUniqueID().toString(), new StorageViewSession(this.itemStorage, player));
			}
		}
	}

	@Override
	public void removeListener(IContainerListener listener) {
		super.removeListener(listener);

		if (this.isStorageCapable()) {
			if (listener instanceof EntityPlayerMP) {
				// Hello player! Let's remove you a StorageViewSession
				EntityPlayerMP player = (EntityPlayerMP) listener;
				this.storageViewSessions.remove(player.getUniqueID().toString());
			}
		}
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (HashMap.Entry<String, StorageViewSession> entry : this.storageViewSessions.entrySet()) {
			entry.getValue().sendUpdate();
		}
	}


	public void handleStorageRequest(EntityPlayerMP player, StorageViewRequestMessage message) {
		if (!this.storageViewSessions.containsKey(message.playerId))
			return;

		StorageViewSession session = this.storageViewSessions.get(message.playerId);

		if (session == null)
			return;

		session.handleStorageRequest(player, message);
	}

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
		if (this.entity instanceof IInventory)
			return ((IInventory)this.entity).isUseableByPlayer(player);
		
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {

		ItemStack previous = null;
		Slot slot = (Slot) this.inventorySlots.get(fromSlot);

	    if (slot == null || !slot.getHasStack())
	    	return previous;

		ItemStack current = slot.getStack();
		previous = current.copy();

		if (slot.inventory == playerIn.inventory) {
			// From Player Inventory to TE Inventory

			if (this.isStorageCapable()) {
				// Send the item into storage instead of whatever slot
				IStorageAccessor accessor = this.itemStorage.getStorageAccessor();

				if (accessor != null) {
					ItemStack remainingItems = accessor.giveItemStack(current, null, false);
					current.stackSize = remainingItems.stackSize;
				}

			} else {
				if (!this.mergeItemStack(current, 0, 9, false))
					return null;
			}
		} else {
			// From TE Inventory to Player Inventory

			if (!this.mergeItemStack(current, 9, 45, true))
				return null;
		}

		if (current.stackSize == 0)
			slot.putStack(null);
		else
			slot.onSlotChanged();

		if (current.stackSize == previous.stackSize)
			return null;
		slot.onPickupFromSlot(playerIn, current);

	    return previous;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (this.entity instanceof IInventory)
			((IInventory)this.entity).closeInventory(playerIn);
	}

	public void handleStorageState(EntityPlayerMP player, StorageViewStateMessage message) {

		if (!this.storageViewSessions.containsKey(message.playerId))
			return;

		StorageViewSession session = this.storageViewSessions.get(message.playerId);

		if (session == null)
			return;

		session.handleStorageState(player, message);
	}

	public void handleRecipeRequest(EntityPlayerMP player, StorageViewRecipeRequestMessage message) {

		Container container = player.openContainer;

		if (!(container instanceof ContainerBase))
			return;

		ContainerBase containerBase = (ContainerBase) container;

		if (!containerBase.isStorageCapable())
			return;

		IStorageTileEntity storageTileEntity = containerBase.getStorageTileEntity();
		IStorageAccessor accessor = storageTileEntity.getStorageAccessor();

		if (!(storageTileEntity instanceof StorageShellEntity))
			return;

		StorageShellEntity storageShell = (StorageShellEntity)storageTileEntity;

		if (accessor == null)
			return;

		if (!storageShell.clearCraftingGrid())
			return;


		List<ItemStack> ingredients = new ArrayList<>();

		boolean first = true;

		for (StorageViewRecipeRequestMessage.SlotOptions slot : message.slots) {

			if (first) {
				first = false;
				continue;
			}

			boolean found = false;

			if (slot.itemOptions.size() == 0) {
				ingredients.add(null);
				continue;
			}

			for (ItemStack option : slot.itemOptions) {
				ItemStack optionTaken = accessor.takeItemStack(option, null, true);

				if (optionTaken != null && optionTaken.stackSize == option.stackSize) {
					ingredients.add(optionTaken);
					found = true;
					break;
				}
			}

			if (!found)
				return;
		}

		int i = 0;
		for (ItemStack ingredient : ingredients) {
			ItemStack taken = null;

			if (ingredient != null) {
				taken = accessor.takeItemStack(ingredient, null, false);

				if (taken.stackSize < ingredient.stackSize) {
					accessor.giveItemStack(taken, null, false);
					return;
				}
			}

			storageShell.setInventorySlotContents(i, taken);
			i += 1;
		}
	}
}