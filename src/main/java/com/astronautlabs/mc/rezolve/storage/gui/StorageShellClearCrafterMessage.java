package com.astronautlabs.mc.rezolve.storage.gui;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StorageShellClearCrafterMessage implements IMessage {

	public StorageShellClearCrafterMessage() {
	}

	public StorageShellClearCrafterMessage(EntityPlayer player) {
		this.playerId = player.getUniqueID().toString();
	}

	public String playerId;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.playerId = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.playerId);
	}
}
