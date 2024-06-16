package org.torchmc.ui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public interface ListViewItem {
    void render(GuiGraphics gfx, int width, int mouseX, int mouseY, float partialTicks);
    int getHeight();

    default boolean isVisible() { return true; }

    default void mouseClicked(int button) {

    }
}
