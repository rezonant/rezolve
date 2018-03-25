package com.astronautlabs.mc.rezolve.network.machines.remoteShell;

import java.util.UUID;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class RemoteShellActivateMessageHandler extends MessageHandler<RemoteShellActivateMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(RemoteShellActivateMessageHandler.class, RemoteShellActivateMessage.class, Side.SERVER);
	}
	
	@Override
	public void handleInGame(RemoteShellActivateMessage message, World world) {
		TileEntity entity = world.getTileEntity(message.getEntityPos());
		
		if (!(entity instanceof RemoteShellEntity)) {
			System.err.println("No bundle builder entity at "+message.getEntityPos().toString());
			return;
		}
		
		RemoteShellEntity remoteShell = (RemoteShellEntity)entity;
		EntityPlayer player = world.getPlayerEntityByUUID(UUID.fromString(message.playerId));
		
		remoteShell.activate(message.activatedMachine, player);
	}
}
