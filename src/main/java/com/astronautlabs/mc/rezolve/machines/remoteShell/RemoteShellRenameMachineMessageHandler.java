package com.astronautlabs.mc.rezolve.machines.remoteShell;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class RemoteShellRenameMachineMessageHandler extends MessageHandler<RemoteShellRenameMachineMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(RemoteShellRenameMachineMessageHandler.class, RemoteShellRenameMachineMessage.class, Side.SERVER);
	}
	
	@Override
	public void handleInGame(RemoteShellRenameMachineMessage message, World world) {
		TileEntity entity = world.getTileEntity(message.getEntityPos());
		
		if (!(entity instanceof RemoteShellEntity)) {
			System.err.println("No bundle builder entity at "+message.getEntityPos().toString());
			return;
		}
		
		RemoteShellEntity remoteShell = (RemoteShellEntity)entity;
		
		remoteShell.renameMachine(message.getMachinePos(), message.getName());
	}
}
