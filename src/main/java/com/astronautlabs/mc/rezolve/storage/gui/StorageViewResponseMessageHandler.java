package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

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
