//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import java.util.UUID;
//
//import com.astronautlabs.mc.rezolve.MessageHandler;
//import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
//
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.api.distmarker.Dist;
//
//public class RemoteShellReturnMessageHandler extends MessageHandler<RemoteShellReturnMessage, IMessage> {
//
//	public static void register() {
//		RezolvePacketHandler.registerMessage(RemoteShellReturnMessageHandler.class, RemoteShellReturnMessage.class, Dist.DEDICATED_SERVER);
//	}
//
//	@Override
//	public void handleInGame(RemoteShellReturnMessage message, Level world) {
//		BlockEntity entity = world.getBlockEntity(message.getEntityPos());
//
//		if (entity == null || !(entity instanceof RemoteShellEntity)) {
//			System.err.println("No remoteShell entity at "+message.getEntityPos().toString());
//			return;
//		}
//
//		RemoteShellEntity remoteShell = (RemoteShellEntity)entity;
//		Player player = world.getPlayerEntityByUUID(UUID.fromString(message.playerId));
//
//		remoteShell.returnToShell(player);
//	}
//}
