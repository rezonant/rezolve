package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets.RemoteShellEntityReturnPacket;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RezolveMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RemoteShellOverlay implements IGuiOverlay {
	public static RemoteShellOverlay INSTANCE;

	public static void register(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("remote_shell", INSTANCE = new RemoteShellOverlay());
	}

	public RemoteShellStatePacket state;
	public void updateState(RemoteShellStatePacket state) {
		this.state = state;
	}

	public RemoteShellOverlay() {
		this.mc = Minecraft.getInstance();
	}

	Minecraft mc;
	//RemoteShellEntity entity;

	@Override
	public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
		if (state == null || !state.active || state.activeMachine == null)
			return;

		this.onMouseEvent(mc.mouseHandler.xpos(), mc.mouseHandler.ypos(), mc.mouseHandler.isLeftPressed());


		Lighting.setupForFlatItems();

		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();

		MachineListing machine = state.activeMachine;
    	BlockPos pos = machine.getBlockPos();
    	ItemStack stack = machine.getItem();

    	int x = 4;
    	int y = 4;

    	if (stack == null) {
    		x += drawText(poseStack, "No activated machine.  ", x, y, 0xFFAA00);
    	} else {
    		x += drawText(poseStack, stack.getDisplayName()+"  ", x, y, 0xFFAA00);
    	}

    	this.remoteShellX = x;
    	this.remoteShellY = y;
    	x += (this.remoteShellWidth = drawText(poseStack, "Remote Shell ", x, 4, this.remoteShellColor));

		var energy = 0; // this.entity.getEnergyStored(Direction.UP) // TODO
    	x += drawText(poseStack, energy+" FE ", x, y, 0xff4f63);

	}

	private int remoteShellX;
	private int remoteShellY;
	private int remoteShellWidth;
	private int fontHeight = 16;
	private int remoteShellColor = 0xbc7100;
	private int remoteShellColorHover = 0xffc300;
	private int remoteShellColorNormal = 0xbc7100;

	private int drawText(PoseStack poseStack, String text, int x, int y, int color) {
		mc.font.draw(poseStack, text, x, y, color);
		return mc.font.width(text);
	}

	private boolean mouseDown = false;

	private void onMouseEvent(double x, double y, boolean clicking) {
		float scaleFactor = (float)mc.getWindow().getGuiScale();
		int mx = (int)(x * scaleFactor);
		int my = (int)((this.mc.getWindow().getHeight() - y) * scaleFactor);

		boolean hitRemoteShell =
			mx > remoteShellX && mx < remoteShellX + remoteShellWidth
			&& my > remoteShellY && my < remoteShellY + fontHeight
		;

		if (!this.mouseDown && clicking) {
			this.mouseDown = true;
			if (hitRemoteShell) {
				System.out.println("You tried to return to the remote shell!");
				var returnMessage = new RemoteShellEntityReturnPacket();
				returnMessage.dimension = state.remoteShellDimension;
				returnMessage.blockPos = state.remoteShellPosition;
				returnMessage.sendToServer();
			}
		} else {
			this.mouseDown = false;
			this.remoteShellColor = hitRemoteShell ? this.remoteShellColorHover : this.remoteShellColorNormal;
		}
	}

}
