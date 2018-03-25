package com.astronautlabs.mc.rezolve.network.machines.securityServer;

import com.astronautlabs.mc.rezolve.RezolveByteBufUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RuleModificationMessage implements IMessage {

	public RuleModificationMessage() { }
	
	public RuleModificationMessage(EntityPlayer player, SecurityServerEntity entity, String ruleId, String ruleName, int mode) {
		
		this.playerId = player.getUniqueID().toString();
		this.entityPos = entity.getPos();
		this.ruleId = ruleId;
		this.ruleName = ruleName;
		this.mode = mode;
	}
	
	String playerId;
	BlockPos entityPos;
	String ruleId;
	String ruleName;
	int mode;
	
	public String getPlayerId() {
		return this.playerId;
	}
	
	public BlockPos getEntityPos() {
		return this.entityPos;
	}
	
	public String getRuleId() {
		return this.ruleId;
	}
	
	public String getRuleName() {
		return this.ruleName;
	}
	
	public int getMode() {
		return this.mode;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.playerId = ByteBufUtils.readUTF8String(buf);
		this.entityPos = RezolveByteBufUtils.readBlockPos(buf);
		this.ruleId = ByteBufUtils.readUTF8String(buf);
		this.ruleName = ByteBufUtils.readUTF8String(buf);
		this.mode = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.playerId);
		RezolveByteBufUtils.writeBlockPos(buf, this.entityPos);
		ByteBufUtils.writeUTF8String(buf, this.ruleId);
		ByteBufUtils.writeUTF8String(buf, this.ruleName);
		buf.writeInt(this.mode);
	}

}
