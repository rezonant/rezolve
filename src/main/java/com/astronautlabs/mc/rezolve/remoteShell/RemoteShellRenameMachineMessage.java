//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import com.astronautlabs.mc.rezolve.RezolveByteBufUtils;
//import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.api.distmarker.Dist;
//
//public class RemoteShellRenameMachineMessage implements IMessage {
//
//	public RemoteShellRenameMachineMessage() { }
//
//	public RemoteShellRenameMachineMessage(RemoteShellEntity entity, BlockPos pos, String name) {
//		this.entityPos = entity.getPos();
//		this.machinePos = pos;
//
//		if (name == null)
//			this.name = "";
//		else
//			this.name = name;
//	}
//
//	BlockPos entityPos;
//	BlockPos machinePos;
//	String name;
//
//	public BlockPos getEntityPos() {
//		return this.entityPos;
//	}
//
//	public BlockPos getMachinePos() {
//		return this.machinePos;
//	}
//
//	public String getName() {
//		return this.name;
//	}
//
//	@Override
//	public void fromBytes(FriendlyByteBuf buf) {
//		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
//		this.machinePos = RezolveByteBufUtils.readBlockPos(buf);
//		this.name = ByteBufUtils.readUTF8String(buf);
//	}
//
//	@Override
//	public void toBytes(FriendlyByteBuf buf) {
//		RezolveByteBufUtils.writeBlockPos(buf, this.entityPos);
//		RezolveByteBufUtils.writeBlockPos(buf, this.machinePos);
//		ByteBufUtils.writeUTF8String(buf, this.name);
//	}
//
//}
