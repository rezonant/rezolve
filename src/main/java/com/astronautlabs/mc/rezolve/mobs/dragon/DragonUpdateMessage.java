package com.astronautlabs.mc.rezolve.inventory;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public class DragonUpdateMessage implements IMessage {
	public DragonUpdateMessage() { }

	public DragonUpdateMessage(int entityId, float headPitch, float headYaw) {
		this.entityId = entityId;
		this.headPitch = headPitch;
		this.headYaw = headYaw;
	}

	public int entityId;
	public float headPitch;
	public float headYaw;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityId = buf.readInt();
		this.headPitch = buf.readFloat();
		this.headYaw = buf.readFloat();

	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeFloat(this.headPitch);
		buf.writeFloat(this.headYaw);
	}
}
