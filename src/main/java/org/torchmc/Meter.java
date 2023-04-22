package org.torchmc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Meter extends WidgetBase {
    public Meter(Font font, int x, int y, int height, Component narrationTitle, Component label, ResourceLocation texture) {
        super(narrationTitle, x, y, 16, height);

        this.font = font;
        this.label = label;
        this.narrationTitle = narrationTitle;
        this.texture = texture;
    }

    private Component label;
    private ResourceLocation texture;
    private Font font;

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
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
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
}
