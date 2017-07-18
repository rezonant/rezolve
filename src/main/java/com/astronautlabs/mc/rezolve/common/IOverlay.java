package com.astronautlabs.mc.rezolve.common;

import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.client.event.MouseEvent;

public interface IOverlay {
	void draw();
	void onMouseEvent(MouseEvent evt);
}
