package com.astronautlabs.mc.rezolve.network.machines.securityServer;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.network.machines.securityServer.SecurityServerEntity.Rule;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class RuleModificationMessageHandler extends MessageHandler<RuleModificationMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(RuleModificationMessageHandler.class, RuleModificationMessage.class, Side.SERVER);
	}
	
	@Override
	public void handleInGame(RuleModificationMessage message, World world) {
		TileEntity entity = world.getTileEntity(message.getEntityPos());
		
		if (!(entity instanceof SecurityServerEntity)) {
			System.err.println("No security server entity at "+message.getEntityPos().toString());
			return;
		}
		
		SecurityServerEntity securityServer = (SecurityServerEntity)entity;
		
		if ("".equals(message.getRuleId())) {

			if ("<players>".equals(message.getRuleName()))
				return;
			if ("<machines>".equals(message.getRuleName()))
				return;
			
			if ("".equals(message.getRuleName()))
				return;
			
			securityServer.addRule(message.getRuleName(), message.getMode());	
		} else {
			if ("".equals(message.getRuleName())) {

				Rule rule = securityServer.getRuleById(message.getRuleId());
				
				if (rule != null) {
					securityServer.removeRule(message.getRuleId());
				}
			} else {
				securityServer.editRule(message.getRuleId(), message.getRuleName(), message.getMode());
			}
		}
	}
}
