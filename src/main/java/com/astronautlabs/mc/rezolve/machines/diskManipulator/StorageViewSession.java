package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class StorageViewSession {
	public StorageViewSession(IItemStorage storage, EntityPlayerMP player) {
		this.storage = storage;
		this.player = player;
	}

	private IItemStorage storage;
	private EntityPlayerMP player;
	private int offset = 0;
	private int limit = 60;
	private String query = "";

	public void sendUpdate() {
		StorageViewMessage message = new StorageViewMessage();

		message.totalItemsCount = this.storage.getTotalItems();
		message.totalStackCount = this.storage.getTotalStacks();
		message.startIndex = this.offset;
		message.items = this.storage.readItems(this.query, this.offset, this.limit);

		this.player.connection.sendPacket(RezolvePacketHandler.INSTANCE.getPacketFrom(message));
	}

	public void handleStorageRequest(EntityPlayerMP player, StorageViewRequestMessage message) {

		StorageViewResponseMessage response = new StorageViewResponseMessage();
		response.operationId = message.operationId;
		response.operationType = message.operationType;
		response.playerId = message.playerId;

		if (StorageViewRequestMessage.OPERATION_GIVE.equals(message.operationType)) {
			ItemStack remainingStack = this.storage.giveItemStack(message.requestedStack, false);
			response.resultingStack = remainingStack;

			player.inventory.setItemStack(remainingStack.stackSize > 0 ? remainingStack : null);
			player.updateHeldItem();

		} else if (StorageViewRequestMessage.OPERATION_TAKE.equals(message.operationType)) {
			ItemStack takenStack = this.storage.takeItemStack(message.requestedStack, false);
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
