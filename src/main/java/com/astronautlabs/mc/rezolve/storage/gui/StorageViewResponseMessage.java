package com.astronautlabs.mc.rezolve.storage.gui;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StorageViewResponseMessage implements IMessage {

	public StorageViewResponseMessage() {
	}

	public StorageViewResponseMessage(String operationId, String operationType, EntityPlayer player, ItemStack stack) {

		StorageViewResponseMessage message = new StorageViewResponseMessage();
		message.operationId = operationId;
		message.operationType = operationType;
		message.playerId = player.getUniqueID().toString();
		message.resultingStack = stack;
	}

	public String operationId;
	public String operationType;
	public String playerId;
	public ItemStack resultingStack;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.playerId = ByteBufUtils.readUTF8String(buf);
		this.operationId = ByteBufUtils.readUTF8String(buf);
		this.operationType = ByteBufUtils.readUTF8String(buf);
		this.resultingStack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.playerId);
		ByteBufUtils.writeUTF8String(buf, this.operationId);
		ByteBufUtils.writeUTF8String(buf, this.operationType);
		ByteBufUtils.writeItemStack(buf, this.resultingStack);
	}
}
