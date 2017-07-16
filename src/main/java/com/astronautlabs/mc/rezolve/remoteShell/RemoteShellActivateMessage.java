package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveByteBufUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RemoteShellActivateMessage implements IMessage {

	public RemoteShellActivateMessage() { }
	
	public RemoteShellActivateMessage(RemoteShellEntity entity, BlockPos activatedMachine, String playerId) {
		this.entityPos = entity.getPos();
		this.activatedMachine = activatedMachine;
		this.playerId = playerId;
	}
	
	BlockPos entityPos;
	BlockPos activatedMachine;
	String playerId;
	
	public BlockPos getEntityPos() {
		return this.entityPos;
	}
	
	public BlockPos getActivatedMachine() {
		return this.activatedMachine;
	}
	
	public String getPlayerId() {
		return this.playerId;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
		this.activatedMachine = RezolveByteBufUtils.readBlockPos(buf);
		this.playerId = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		RezolveByteBufUtils.writeBlockPos(buf, this.entityPos);
		RezolveByteBufUtils.writeBlockPos(buf, this.activatedMachine);
		ByteBufUtils.writeUTF8String(buf, this.playerId);
	}

}
