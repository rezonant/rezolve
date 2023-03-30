//package com.astronautlabs.mc.rezolve.remoteShell;
//
//import org.lwjgl.input.Mouse;
//
//import com.astronautlabs.mc.rezolve.common.IOverlay;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Gui;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.GlStateManager;
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.core.Direction;
//import net.minecraft.core.BlockPos;
//import net.minecraftforge.client.event.MouseEvent;
//
//public class RemoteShellOverlay extends Gui implements IOverlay {
//
//	public RemoteShellOverlay(RemoteShellEntity entity) {
//		this.mc = Minecraft.getInstance();
//		this.entity = entity;
//	}
//
//	Minecraft mc;
//	RemoteShellEntity entity;
//
//	@Override
//	public void draw() {
//
//		GlStateManager.disableLighting();
//    	ScaledResolution scaled = new ScaledResolution(mc);
//    	int width = scaled.getScaledWidth();
//    	int height = scaled.getScaledHeight();
//
//    	BlockPos pos = this.entity.getClientActivatedMachine();
//    	ItemStack stack = null;
//
//    	if (pos != null)
//    		stack = RemoteShellScreen.getItemFromBlock(this.entity, pos);
//
//    	int x = 4;
//    	int y = 4;
//
//    	if (stack == null) {
//    		x += drawText("No activated machine.  ", x, y, 0xFFAA00);
//    	} else {
//    		x += drawText(stack.getDisplayName()+"  ", x, y, 0xFFAA00);
//    	}
//
//    	this.remoteShellX = x;
//    	this.remoteShellY = y;
//    	x += (this.remoteShellWidth = drawText("Remote Shell ", x, 4, this.remoteShellColor));
//
//    	x += drawText(this.entity.getEnergyStored(Direction.UP) +" RF ", x, y, 0xff4f63);
//
//	}
//
//	private int remoteShellX;
//	private int remoteShellY;
//	private int remoteShellWidth;
//	private int fontHeight = 16;
//	private int remoteShellColor = 0xbc7100;
//	private int remoteShellColorHover = 0xffc300;
//	private int remoteShellColorNormal = 0xbc7100;
//
//	private int drawText(String text, int x, int y, int color) {
//		drawString(mc.fontRendererObj, text, x, y, color);
//		return mc.fontRendererObj.getStringWidth(text);
//	}
//
//	private boolean mouseDown = false;
//
//	@Override
//	public void onMouseEvent(MouseEvent evt) {
//
//		ScaledResolution scale = new ScaledResolution(this.mc);
//
//		float scaleFactor = scale.getScaledWidth() / (float)this.mc.displayWidth;
//		int mx = (int)(evt.getX() * scaleFactor);
//		int my = (int)((this.mc.displayHeight - evt.getY()) * scaleFactor);
//		int btn = evt.getButton();
//
//		boolean hitRemoteShell =
//			mx > remoteShellX && mx < remoteShellX + remoteShellWidth
//			&& my > remoteShellY && my < remoteShellY + fontHeight
//		;
//
//		if (!this.mouseDown && Mouse.isButtonDown(0)) {
//			this.mouseDown = true;
//			if (hitRemoteShell) {
//				System.out.println("You tried to return to the remote shell!");
//				this.entity.returnToShell();
//			}
//		} else {
//			this.mouseDown = false;
//			this.remoteShellColor = hitRemoteShell ? this.remoteShellColorHover : this.remoteShellColorNormal;
//		}
//	}
//
//}
