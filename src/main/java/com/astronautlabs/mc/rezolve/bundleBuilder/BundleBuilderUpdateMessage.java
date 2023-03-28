//package com.astronautlabs.mc.rezolve.bundleBuilder;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//
//public class BundleBuilderUpdateMessage implements IMessage {
//
//	public BundleBuilderUpdateMessage() { }
//
//	public BundleBuilderUpdateMessage(BundleBuilderEntity entity) {
//		BlockPos pos = entity.getPos();
//		this.x = pos.getX();
//		this.y = pos.getY();
//		this.z = pos.getZ();
//
//		this.patternName = entity.getPatternName();
//	}
//
//	int x;
//	int y;
//	int z;
//	String patternName;
//
//	public String getPatternName() {
//		return this.patternName;
//	}
//
//	public BlockPos getEntityPos() {
//		return new BlockPos(this.x, this.y, this.z);
//	}
//
//	@Override
//	public void fromBytes(FriendlyByteBuf buf) {
//		this.x = buf.readInt();
//		this.y = buf.readInt();
//		this.z = buf.readInt();
//		this.patternName = ByteBufUtils.readUTF8String(buf);
//	}
//
//	@Override
//	public void toBytes(FriendlyByteBuf buf) {
//		buf.writeInt(this.x);
//		buf.writeInt(this.y);
//		buf.writeInt(this.z);
//		ByteBufUtils.writeUTF8String(buf, this.patternName);
//	}
//
//}
