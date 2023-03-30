//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import com.astronautlabs.mc.rezolve.util.RezolveByteBufUtils;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//
//public class RemoteShellActivateMessage implements IMessage {
//
//	public RemoteShellActivateMessage() { }
//
//	public RemoteShellActivateMessage(RemoteShellEntity entity, BlockPos activatedMachine, String playerId) {
//		this.entityPos = entity.getBlockPos();
//		this.activatedMachine = activatedMachine;
//		this.playerId = playerId;
//	}
//
//	BlockPos entityPos;
//	BlockPos activatedMachine;
//	String playerId;
//
//	public BlockPos getEntityPos() {
//		return this.entityPos;
//	}
//
//	public BlockPos getActivatedMachine() {
//		return this.activatedMachine;
//	}
//
//	public String getPlayerId() {
//		return this.playerId;
//	}
//
//	@Override
//	public void fromBytes(FriendlyByteBuf buf) {
//		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
//		this.activatedMachine = RezolveByteBufUtils.readBlockPos(buf);
//		this.playerId = buf.readUtf();
//	}
//
//	@Override
//	public void toBytes(FriendlyByteBuf buf) {
//		buf.writeBlockPos(this.entityPos);
//		buf.writeBlockPos(this.activatedMachine);
//		buf.writeUtf(this.playerId);
//	}
//
//}
