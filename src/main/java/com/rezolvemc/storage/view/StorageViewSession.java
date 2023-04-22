package com.rezolvemc.storage.view;

import com.rezolvemc.storage.IStorageAccessor;
import com.rezolvemc.storage.IStorageTileEntity;
import com.rezolvemc.util.ItemStackUtil;
import com.rezolvemc.storage.view.packets.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

/**
 * Provides the server-side session for an open StorageView.
 */
public class StorageViewSession {
	public StorageViewSession(AbstractContainerMenu menu, IStorageTileEntity storage, ServerPlayer player) {
		this.storage = storage;
		this.player = player;
		this.menu = menu;
	}

	private AbstractContainerMenu menu;
	private IStorageTileEntity storage;
	private ServerPlayer player;
	private int offset = 0;
	private int limit = 60;
	private String query = "";
	private IStorageAccessor accessor;

	public void sendUpdate() {
		accessor = this.storage.getStorageAccessor();
		StorageViewContentPacket message = new StorageViewContentPacket();

		message.setMenu(menu);

		if (accessor != null) {
			message.totalItemsCount = accessor.getTotalItems();
			message.totalStackCount = accessor.getTotalStacks();
			message.startIndex = this.offset;
			message.setItems(accessor.readItems(this.query, this.offset, this.limit));
		} else {
			message.totalItemsCount = 0;
			message.totalStackCount = 0;
			message.startIndex = 0;
			message.setItems(new ArrayList<>());
		}

		message.sendToPlayer(player);
	}

	public void checkForUpdates() {
		if (storage.getStorageAccessor() != accessor) {
			sendUpdate();
		}
	}

	public ItemStack giveItem(ItemStack stack, boolean simulate) {
		IStorageAccessor accessor = this.storage.getStorageAccessor();
		if (accessor == null)
			return stack;

		var result = accessor.giveItemStack(stack, ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack)), simulate);

		sendUpdate();

		return result;
	}

	public ItemStack takeItem(ItemStack stack, boolean simulate) {
		var result = accessor.takeItemStack(stack, ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack)), simulate);

		sendUpdate();

		return result;
	}

	public void handleStorageRequest(StorageViewChangeRequest message) {

		IStorageAccessor accessor = this.storage.getStorageAccessor();
		if (accessor == null)
			return;

		StorageViewChangeResponse response = new StorageViewChangeResponse();
		response.setMenu(menu);
		response.operationId = message.operationId;
		response.operationType = message.operationType;
		response.playerId = message.playerId;

		if (StorageViewChangeRequest.OPERATION_GIVE.equals(message.operationType)) {


			ItemStack stack = message.requestedStack;
			String hash = message.requestedStackHash;

			if ("hand".equals(hash)) {
				stack = player.containerMenu.getCarried();
				hash = ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack));
			}

			ItemStack remainingStack = accessor.giveItemStack(stack, hash, false);
			response.resultingStack = remainingStack;

			player.containerMenu.setCarried(remainingStack.getCount() > 0 ? remainingStack : ItemStack.EMPTY);
			//player.updateHeldItem();

		} else if (StorageViewChangeRequest.OPERATION_TAKE.equals(message.operationType)) {
			ItemStack takenStack = accessor.takeItemStack(message.requestedStack, message.requestedStackHash, false);
			response.resultingStack = takenStack;

			player.containerMenu.setCarried(takenStack.getCount() > 0 ? takenStack : ItemStack.EMPTY);
			//player.updateHeldItem();

		} else {
			System.out.println("ERROR: Unknown StorageView operation '" + message.operationType + "': Skipping!");
			return;
		}

		response.sendToPlayer(player);
		sendUpdate();
	}

	public void handleQuery(StorageViewQuery message) {
		this.offset = message.offset;
		this.limit = message.limit;
		this.query = message.query;
		this.sendUpdate();
	}

	public void handlePacket(StorageViewPacket storageViewPacket) {
		if (storageViewPacket instanceof StorageViewQuery query)
			handleQuery(query);
		else if (storageViewPacket instanceof StorageViewChangeRequest change)
			handleStorageRequest(change);
	}
}
