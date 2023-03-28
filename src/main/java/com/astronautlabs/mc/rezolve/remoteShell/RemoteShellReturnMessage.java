//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import com.astronautlabs.mc.rezolve.RezolveByteBufUtils;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//
//public class RemoteShellReturnMessage implements IMessage {
//
//	public RemoteShellReturnMessage() { }
//
//	public RemoteShellReturnMessage(RemoteShellEntity entity, String playerId) {
//		this.entityPos = entity.getPos();
//		this.playerId = playerId;
//	}
//
//	BlockPos entityPos;
//	String playerId;
//
//	public BlockPos getEntityPos() {
//		return this.entityPos;
//	}
//
//	public String getPlayerId() {
//		return this.playerId;
//	}
//
//	@Override
//	public void fromBytes(FriendlyByteBuf buf) {
//		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
//		this.playerId = ByteBufUtils.readUTF8String(buf);
//	}
//
//	@Override
//	public void toBytes(FriendlyByteBuf buf) {
//		RezolveByteBufUtils.writeBlockPos(buf, this.entityPos);
//		ByteBufUtils.writeUTF8String(buf, this.playerId);
//	}
//
//}
