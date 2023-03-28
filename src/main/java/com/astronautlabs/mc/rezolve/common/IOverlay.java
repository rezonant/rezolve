package com.astronautlabs.mc.rezolve.common;

import net.minecraftforge.client.event.InputEvent;

public interface IOverlay {
	void draw();
	void onMouseEvent(InputEvent evt);
}
