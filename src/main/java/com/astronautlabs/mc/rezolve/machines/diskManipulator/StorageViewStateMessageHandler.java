package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class StorageViewStateMessageHandler extends MessageHandler<StorageViewStateMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(StorageViewStateMessageHandler.class, StorageViewStateMessage.class, Side.SERVER);
	}

	@Override
	public void handleInGame(StorageViewStateMessage message, World world) {

		EntityPlayerMP player = (EntityPlayerMP)world.getPlayerEntityByUUID(UUID.fromString(message.playerId));

		if (player.openContainer instanceof ContainerBase) {
			ContainerBase containerBase = (ContainerBase)player.openContainer;
			containerBase.handleStorageState(player, message);
		}
	}
}
