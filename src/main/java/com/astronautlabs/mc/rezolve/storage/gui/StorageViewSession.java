package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.storage.IItemStorage;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class StorageViewSession {
	public StorageViewSession(IStorageTileEntity storage, EntityPlayerMP player) {
		this.storage = storage;
		this.player = player;
	}

	private IStorageTileEntity storage;
	private EntityPlayerMP player;
	private int offset = 0;
	private int limit = 60;
	private String query = "";

	public void sendUpdate() {
		IStorageAccessor accessor = this.storage.getStorageAccessor();
		if (accessor == null)
			return;

		StorageViewMessage message = new StorageViewMessage();
		message.totalItemsCount = accessor.getTotalItems();
		message.totalStackCount = accessor.getTotalStacks();
		message.startIndex = this.offset;
		message.setItems(accessor.readItems(this.query, this.offset, this.limit));

		this.player.connection.sendPacket(RezolvePacketHandler.INSTANCE.getPacketFrom(message));
	}

	public void handleStorageRequest(EntityPlayerMP player, StorageViewRequestMessage message) {

		IStorageAccessor accessor = this.storage.getStorageAccessor();
		if (accessor == null)
			return;

		StorageViewResponseMessage response = new StorageViewResponseMessage();
		response.operationId = message.operationId;
		response.operationType = message.operationType;
		response.playerId = message.playerId;

		if (StorageViewRequestMessage.OPERATION_GIVE.equals(message.operationType)) {

			ItemStack stack = message.requestedStack;
			String hash = message.requestedStackHash;

			if ("hand".equals(hash)) {
				stack = player.inventory.getItemStack();
				hash = ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack));
			}

			ItemStack remainingStack = accessor.giveItemStack(stack, hash, false);
			response.resultingStack = remainingStack;

			player.inventory.setItemStack(remainingStack.stackSize > 0 ? remainingStack : null);
			player.updateHeldItem();

		} else if (StorageViewRequestMessage.OPERATION_TAKE.equals(message.operationType)) {
			ItemStack takenStack = accessor.takeItemStack(message.requestedStack, message.requestedStackHash, false);
			response.resultingStack = takenStack;

			player.inventory.setItemStack(takenStack.stackSize > 0 ? takenStack : null);
			player.updateHeldItem();

		} else {
			System.out.println("ERROR: Unknown StorageView operation '" + message.operationType + "': Skipping!");
			return;
		}

		player.connection.sendPacket(RezolvePacketHandler.INSTANCE.getPacketFrom(response));
	}

	public void handleStorageState(EntityPlayerMP player, StorageViewStateMessage message) {
		this.offset = message.offset;
		this.limit = message.limit;
		this.query = message.query;
		this.sendUpdate();
	}
}
