//package com.astronautlabs.mc.rezolve.securityServer;
//
//import com.astronautlabs.mc.rezolve.MessageHandler;
//import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
//import com.astronautlabs.mc.rezolve.securityServer.SecurityServerEntity.Rule;
//
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.api.distmarker.Dist;
//
//public class RuleModificationMessageHandler extends MessageHandler<RuleModificationMessage, IMessage> {
//
//	public static void register() {
//		RezolvePacketHandler.registerMessage(RuleModificationMessageHandler.class, RuleModificationMessage.class, Dist.DEDICATED_SERVER);
//	}
//
//	@Override
//	public void handleInGame(RuleModificationMessage message, Level world) {
//		BlockEntity entity = world.getBlockEntity(message.getEntityPos());
//
//		if (!(entity instanceof SecurityServerEntity)) {
//			System.err.println("No security server entity at "+message.getEntityPos().toString());
//			return;
//		}
//
//		SecurityServerEntity securityServer = (SecurityServerEntity)entity;
//
//		if ("".equals(message.getRuleId())) {
//
//			if ("<players>".equals(message.getRuleName()))
//				return;
//			if ("<machines>".equals(message.getRuleName()))
//				return;
//
//			if ("".equals(message.getRuleName()))
//				return;
//
//			securityServer.addRule(message.getRuleName(), message.getMode());
//		} else {
//			if ("".equals(message.getRuleName())) {
//
//				Rule rule = securityServer.getRuleById(message.getRuleId());
//
//				if (rule != null) {
//					securityServer.removeRule(message.getRuleId());
//				}
//			} else {
//				securityServer.editRule(message.getRuleId(), message.getRuleName(), message.getMode());
//			}
//		}
//	}
//}
