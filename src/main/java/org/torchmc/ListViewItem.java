package org.torchmc;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ListViewItem {
    void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks);
    int getHeight();

    default void mouseClicked(int button) {

    }
}
