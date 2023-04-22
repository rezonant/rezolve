package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TextListViewItem implements ListViewItem {
    public TextListViewItem(Component content) {
        this.content = content;
    }

    public TextListViewItem(String string) {
        this.content = Component.literal(string);
    }

    Component content;

    @Override
    public void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().font.draw(poseStack, content, 0, 0, 0xFF000000);
    }

    @Override
    public int getHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }
}
