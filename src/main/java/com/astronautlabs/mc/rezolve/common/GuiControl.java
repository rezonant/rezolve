package com.astronautlabs.mc.rezolve.common;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public class GuiControl extends Gui {
	public GuiControl(GuiScreen screen, int x, int y, int width, int height) {
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public GuiControl(GuiScreen screen) {
		this.screen = screen;
	}

	protected GuiScreen screen;
	protected int x;
	protected int y;
	protected int width;
	protected int height;

	public GuiScreen getScreen() {
		return this.screen;
	}

	public void render(int mouseX, int mouseY) {
	}

	public void handleMouseInput() {

	}

	public void onClick(int x, int y, int mouseButton) {
	}

	public void moveAndResize(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void move(int x, int y) {
		this.moveAndResize(x, y, this.width, this.height);
	}

	public void resize(int width, int height) {
		this.moveAndResize(this.x, this.y, width, height);
	}

	public void renderOverlay(int mouseX, int mouseY) {

	}
}
