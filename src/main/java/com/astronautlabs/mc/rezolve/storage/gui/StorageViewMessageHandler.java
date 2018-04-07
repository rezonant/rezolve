package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.MessageHandler;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class StorageViewMessageHandler extends MessageHandler<StorageViewMessage, IMessage> {

	public static void register() {
		RezolvePacketHandler.registerMessage(StorageViewMessageHandler.class, StorageViewMessage.class, Side.CLIENT);
	}

	@Override
	public void handleInGame(StorageViewMessage message, World world) {
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;

		if (screen == null) {
			System.out.println("No active screen, the storage view update must be late. Discarding.");
			return;
		}

		if (screen instanceof IStorageViewContainer) {
			StorageView storageView = ((IStorageViewContainer) screen).getStorageView();
			storageView.handleUpdate(message);
		} else {
			// When JEI is activated when the storage view is active, this message floods.
			// Not sure of a good way to filter this out so that the message still has some use for debugging.

			//System.out.println("No active storage veiw container, the storage view update must be late. Discarding.");
			return;
		}
	}
}
