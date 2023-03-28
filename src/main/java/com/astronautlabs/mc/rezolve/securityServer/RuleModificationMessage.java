//package com.astronautlabs.mc.rezolve.securityServer;
//
//import com.astronautlabs.mc.rezolve.RezolveByteBufUtils;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//
//public class RuleModificationMessage implements IMessage {
//
//	public RuleModificationMessage() { }
//
//	public RuleModificationMessage(Player player, SecurityServerEntity entity, String ruleId, String ruleName, int mode) {
//
//		this.playerId = player.getUniqueID().toString();
//		this.entityPos = entity.getPos();
//		this.ruleId = ruleId;
//		this.ruleName = ruleName;
//		this.mode = mode;
//	}
//
//	String playerId;
//	BlockPos entityPos;
//	String ruleId;
//	String ruleName;
//	int mode;
//
//	public String getPlayerId() {
//		return this.playerId;
//	}
//
//	public BlockPos getEntityPos() {
//		return this.entityPos;
//	}
//
//	public String getRuleId() {
//		return this.ruleId;
//	}
//
//	public String getRuleName() {
//		return this.ruleName;
//	}
//
//	public int getMode() {
//		return this.mode;
//	}
//
//	@Override
//	public void fromBytes(FriendlyByteBuf buf) {
//		this.playerId = ByteBufUtils.readUTF8String(buf);
//		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
//		this.ruleId = ByteBufUtils.readUTF8String(buf);
//		this.ruleName = ByteBufUtils.readUTF8String(buf);
//		this.mode = buf.readInt();
//	}
//
//	@Override
//	public void toBytes(FriendlyByteBuf buf) {
//		ByteBufUtils.writeUTF8String(buf, this.playerId);
//		RezolveByteBufUtils.writeBlockPos(buf, this.entityPos);
//		ByteBufUtils.writeUTF8String(buf, this.ruleId);
//		ByteBufUtils.writeUTF8String(buf, this.ruleName);
//		buf.writeInt(this.mode);
//	}
//
//}
