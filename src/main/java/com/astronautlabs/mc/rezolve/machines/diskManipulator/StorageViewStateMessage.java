package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StorageViewStateMessage implements IMessage {

	public StorageViewStateMessage() {

	}

	public StorageViewStateMessage(EntityPlayer player, String query, int offset, int limit) {
		this.playerId = player.getUniqueID().toString();
		this.query = query;
		this.offset = offset;
		this.limit = limit;
	}

	public String playerId;
	public String query;
	public int offset;
	public int limit;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.playerId = ByteBufUtils.readUTF8String(buf);
		this.query = ByteBufUtils.readUTF8String(buf);
		this.offset  = buf.readInt();
		this.limit = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.playerId);
		ByteBufUtils.writeUTF8String(buf, this.query);
		buf.writeInt(this.offset);
		buf.writeInt(this.limit);
	}
}
