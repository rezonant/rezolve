package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class StorageViewRequestMessageHandler extends MessageHandler<StorageViewRequestMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(StorageViewRequestMessageHandler.class, StorageViewRequestMessage.class, Side.SERVER);
	}

	@Override
	public void handleInGame(StorageViewRequestMessage message, World world) {
		EntityPlayerMP player = (EntityPlayerMP)world.getPlayerEntityByUUID(UUID.fromString(message.playerId));

		Container container = player.openContainer;

		if (container instanceof ContainerBase) {
			ContainerBase containerBase = (ContainerBase)container;
			containerBase.handleStorageRequest(player, message);
		}
	}
}
