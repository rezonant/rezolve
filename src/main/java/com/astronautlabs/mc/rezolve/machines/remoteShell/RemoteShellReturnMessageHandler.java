package com.astronautlabs.mc.rezolve.machines.remoteShell;

import java.util.UUID;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class RemoteShellReturnMessageHandler extends MessageHandler<RemoteShellReturnMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(RemoteShellReturnMessageHandler.class, RemoteShellReturnMessage.class, Side.SERVER);
	}
	
	@Override
	public void handleInGame(RemoteShellReturnMessage message, World world) {
		TileEntity entity = world.getTileEntity(message.getEntityPos());
		
		if (entity == null || !(entity instanceof RemoteShellEntity)) {
			System.err.println("No remoteShell entity at "+message.getEntityPos().toString());
			return;
		}
		
		RemoteShellEntity remoteShell = (RemoteShellEntity)entity;
		EntityPlayer player = world.getPlayerEntityByUUID(UUID.fromString(message.playerId));
		
		remoteShell.returnToShell(player);
	}
}
