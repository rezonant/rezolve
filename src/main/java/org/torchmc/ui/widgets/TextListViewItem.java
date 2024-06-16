package org.torchmc.ui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics gfx, int width, int mouseX, int mouseY, float partialTicks) {
        gfx.drawString(Minecraft.getInstance().font, content, 0, 0, 0xFF000000, false);
    }

    @Override
    public int getHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }
}
