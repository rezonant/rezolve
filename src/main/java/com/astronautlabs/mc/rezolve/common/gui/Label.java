package com.astronautlabs.mc.rezolve.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class Label extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public Label(Font font, Component initialContent, int x, int y, int width) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;

        setContent(initialContent);
    }

    public static final int DEFAULT_COLOR = 4210752;

    public Label(Font font, Component initialContent, int x, int y) {
        this(font, initialContent, x, y, 99999);
    }

    private Font font;
    private int x;
    private int y;
    private int width;
    private MultiLineLabel label;
    private Alignment alignment = Alignment.LEFT;
    private Component content;
    private int color = DEFAULT_COLOR;

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, content);
    }

    public enum Alignment {
        LEFT,
        CENTERED
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public FormattedText getContent() {
        return content;
    }

    public void setContent(Component content) {
        this.content = content;
        this.label = MultiLineLabel.create(font, content, width);
    }

    boolean visible = true;

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!isVisible())
            return;

        if (alignment == Alignment.CENTERED)
            label.renderCentered(pPoseStack, width, x, y, color);
        else if (alignment == Alignment.LEFT)
            label.renderLeftAlignedNoShadow(pPoseStack, x, y, font.lineHeight, color);

    }
}
