package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class StorageViewRequestMessage implements IMessage {

	public static final String OPERATION_TAKE = "t";
	public static final String OPERATION_GIVE = "g";

	public StorageViewRequestMessage() {
	}

	public static StorageViewRequestMessage takeItems(EntityPlayer player, ItemStack stack) {
		StorageViewRequestMessage message = new StorageViewRequestMessage();
		message.operationId = UUID.randomUUID().toString();
		message.playerId = player.getUniqueID().toString();
		message.operationType = OPERATION_TAKE;
		message.requestedStack = stack;

		return message;
	}

	public static StorageViewRequestMessage giveItems(EntityPlayer player, ItemStack stack) {
		StorageViewRequestMessage message = new StorageViewRequestMessage();
		message.operationId = UUID.randomUUID().toString();
		message.playerId = player.getUniqueID().toString();
		message.operationType = OPERATION_GIVE;
		message.requestedStack = stack;

		return message;
	}

	public String playerId;
	public String operationId;
	public String operationType;
	public ItemStack requestedStack;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.playerId = ByteBufUtils.readUTF8String(buf);
		this.operationId = ByteBufUtils.readUTF8String(buf);
		this.operationType = ByteBufUtils.readUTF8String(buf);
		this.requestedStack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.playerId);
		ByteBufUtils.writeUTF8String(buf, this.operationId);
		ByteBufUtils.writeUTF8String(buf, this.operationType);
		ByteBufUtils.writeItemStack(buf, this.requestedStack);
	}
}
