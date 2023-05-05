package org.torchmc.ui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ListViewItem {
    void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks);
    int getHeight();

    default boolean isVisible() { return true; }

    default void mouseClicked(int button) {

    }
}
