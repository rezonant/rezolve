package org.torchmc.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.torchmc.ui.util.TorchUtil;

/**
 * A window which prevents interacting with other windows while it is being presented.
 * Other windows are faded out to indicate to the user that action must be taken.
 */
public class ModalWindow extends Window {
    public ModalWindow(Component title) {
        super(title);
    }

    public ModalWindow(String title) {
        super(title);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return isVisible();
    }

    @Override
    protected void renderBackground(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        TorchUtil.colorQuad(gfx, 0x80000000, 0, 0, screen.width, screen.height);
        super.renderBackground(gfx, pMouseX, pMouseY, pPartialTick);
    }
}
