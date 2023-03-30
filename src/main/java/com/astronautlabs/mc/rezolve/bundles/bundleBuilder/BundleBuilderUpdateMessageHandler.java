//package com.astronautlabs.mc.rezolve.bundleBuilder;
//
//import com.astronautlabs.mc.rezolve.MessageHandler;
//import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
//
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.api.distmarker.Dist;
//
//public class BundleBuilderUpdateMessageHandler extends MessageHandler<BundleBuilderUpdateMessage, IMessage> {
//
//	public static void register() {
//		RezolvePacketHandler.registerMessage(BundleBuilderUpdateMessageHandler.class, BundleBuilderUpdateMessage.class, Dist.DEDICATED_SERVER);
//	}
//
//	@Override
//	public void handleInGame(BundleBuilderUpdateMessage message, Level world) {
//		BlockEntity entity = world.getBlockEntity(message.getEntityPos());
//
//		if (!(entity instanceof BundleBuilderEntity)) {
//			System.err.println("No bundle builder entity at "+message.getEntityPos().toString());
//			return;
//		}
//
//		BundleBuilderEntity bundleBuilder = (BundleBuilderEntity)entity;
//		bundleBuilder.setPatternName(message.getPatternName());
//	}
//}
