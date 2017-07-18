package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveByteBufUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RemoteShellReturnMessage implements IMessage {

	public RemoteShellReturnMessage() { }
	
	public RemoteShellReturnMessage(RemoteShellEntity entity, String playerId) {
		this.entityPos = entity.getPos();
		this.playerId = playerId;
	}
	
	BlockPos entityPos;
	String playerId;
	
	public BlockPos getEntityPos() {
		return this.entityPos;
	}
	
	public String getPlayerId() {
		return this.playerId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
		this.playerId = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		RezolveByteBufUtils.writeBlockPos(buf, this.entityPos);
		ByteBufUtils.writeUTF8String(buf, this.playerId);
	}

}
