package com.astronautlabs.mc.rezolve;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;

public abstract class MessagePacket<T extends INetHandler> implements Packet<T>, IMessage {
	public MessagePacket() { }

	public abstract void writeToNBT(NBTTagCompound compound);
	public abstract void readFromNBT(NBTTagCompound compound);

	/**
	 * Reads the raw packet data from the data stream.
	 */
	public void readPacketData(PacketBuffer buf) {
		NBTTagCompound nbt = null;
		try {
			nbt = buf.readNBTTagCompoundFromBuffer();
		} catch (IOException e) {
			System.err.println("Rezolve Mod: Caught IOException while reading payload of data packet!");
			e.printStackTrace();
		}

		this.readFromNBT(nbt);
	}

	/**
	 * Writes the raw packet data to the data stream.
	 */
	public void writePacketData(PacketBuffer buf) {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		buf.writeNBTTagCompoundToBuffer(nbt);
	}

	@Override
	public void processPacket(T handler) {

	}

	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound nbt = ByteBufUtils.readTag(buf);
		this.readFromNBT(nbt);

	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		ByteBufUtils.writeTag(buf, nbt);
	}
}
