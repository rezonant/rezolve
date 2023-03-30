//package com.astronautlabs.mc.rezolve.common;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//
//public class GhostSlotUpdateMessage implements IMessage {
//
//
//	public GhostSlotUpdateMessage() { }
//
//	public GhostSlotUpdateMessage(EntityPlayerSP player, BlockEntity entity, int slot, ItemStack itemStack) {
//		this.playerId = player.getUniqueID().toString();
//
//		BlockPos pos = entity.getPos();
//		this.x = pos.getX();
//		this.y = pos.getY();
//		this.z = pos.getZ();
//
//		this.slot = slot;
//		this.stack = itemStack;
//	}
//
//	String playerId;
//	int x;
//	int y;
//	int z;
//	int slot;
//	ItemStack stack;
//
//	public ItemStack getStack() {
//		return this.stack;
//	}
//
//	public BlockPos getEntityPos() {
//		return new BlockPos(this.x, this.y, this.z);
//	}
//
//	@Override
//	public void fromBytes(FriendlyByteBuf buf) {
//		this.playerId = ByteBufUtils.readUTF8String(buf);
//		this.x = buf.readInt();
//		this.y = buf.readInt();
//		this.z = buf.readInt();
//		this.slot = buf.readInt();
//		this.stack = ByteBufUtils.readItemStack(buf);
//
//	}
//
//	@Override
//	public void toBytes(FriendlyByteBuf buf) {
//		ByteBufUtils.writeUTF8String(buf, this.playerId);
//		buf.writeInt(this.x);
//		buf.writeInt(this.y);
//		buf.writeInt(this.z);
//		buf.writeInt(this.slot);
//		ByteBufUtils.writeItemStack(buf, this.stack);
//	}
//}
