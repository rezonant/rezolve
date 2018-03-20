package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class StorageViewResponseMessageHandler extends MessageHandler<StorageViewResponseMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(StorageViewResponseMessageHandler.class, StorageViewResponseMessage.class, Side.CLIENT);
	}

	@Override
	public void handleInGame(StorageViewResponseMessage message, World world) {

		GuiScreen screen = Minecraft.getMinecraft().currentScreen;

		if (screen != null && screen instanceof IStorageViewContainer) {
			IStorageViewContainer storageViewContainer = (IStorageViewContainer)screen;
			StorageView storageView = storageViewContainer.getStorageView();
			storageView.handleResponse(message);


		}
	}
}
