package com.astronautlabs.mc.rezolve.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Meter extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public Meter(Font font, int x, int y, int height, Component narrationTitle, Component label, ResourceLocation texture) {
        this.font = font;
        this.label = label;
        this.narrationTitle = narrationTitle;
        this.texture = texture;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    private int x;
    private int y;
    private int width = 16;
    private int height;
    private Component label;
    private Component narrationTitle;
    private ResourceLocation texture;
    private Font font;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    double max = 1;
    double value = 0.5;
    double min = 0;

    public double getMax() {
        return max;
    }

    public double getValue() {
        return value;
    }

    public double getMin() {
        return min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private float getRenderedValue() {
        return (float)((value - min) / (max - min));
    }

    public void updateState() {
        // Implement in subclass
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        updateState();

        var borderSize = 2.0f;
        var textureHeight = 16.0f;
        var labelMargin = 5;
        var labelAreaHeight = font.lineHeight + labelMargin*2;
        var barHeight = height - labelAreaHeight;

        RezolveGuiUtil.textureQuad(
                pPoseStack, texture, x, y, width, borderSize,
                0, 0, 1, borderSize/textureHeight
        );
        RezolveGuiUtil.textureQuad(
                pPoseStack, texture, x, y + borderSize, width, barHeight - borderSize*2,
                0, borderSize/textureHeight,
                1, (textureHeight - borderSize)/textureHeight
        );
        RezolveGuiUtil.textureQuad(
                pPoseStack, texture, x, y + barHeight - borderSize, width, borderSize,
                0, (textureHeight - borderSize)/textureHeight,
                1, 1
        );

        // Black out the unfilled portion
        RezolveGuiUtil.colorQuad(pPoseStack, 0, 0, 0, 1, x, y, width, barHeight - getRenderedValue() * barHeight);

        // Label

        var labelWidth = font.width(label);
        font.draw(pPoseStack, label, x + width / 2 - labelWidth / 2, y + barHeight + labelMargin, 0xFF333333);

    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, narrationTitle);
    }

    public int getWidth() {
        return width;
    }
}
