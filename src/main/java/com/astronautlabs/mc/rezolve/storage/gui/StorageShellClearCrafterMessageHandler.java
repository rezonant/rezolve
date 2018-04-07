package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.storage.machines.storageShell.StorageShellEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class StorageShellClearCrafterMessageHandler extends MessageHandler<StorageShellClearCrafterMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(StorageShellClearCrafterMessageHandler.class, StorageShellClearCrafterMessage.class, Side.SERVER);
	}

	@Override
	public void handleInGame(StorageShellClearCrafterMessage message, World world) {
		EntityPlayerMP player = (EntityPlayerMP)world.getPlayerEntityByUUID(UUID.fromString(message.playerId));
		Container container = player.openContainer;

		if (container instanceof ContainerBase) {
			ContainerBase containerBase = (ContainerBase)container;
			TileEntity entity = containerBase.getEntity();

			if (entity instanceof StorageShellEntity)
				((StorageShellEntity) entity).clearCraftingGrid();
		}
	}
}
