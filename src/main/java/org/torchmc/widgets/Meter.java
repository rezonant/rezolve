package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.torchmc.WidgetBase;
import org.torchmc.layout.Axis;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Color;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

public class Meter extends WidgetBase {
    public static final int TEXTURE_HEIGHT = 16;

    public Meter(Component narrationTitle, Component label, ResourceLocation texture) {
        super(narrationTitle);

        this.label = label;
        this.narrationTitle = narrationTitle;
        this.texture = texture;
        this.width = 16;
        this.height = 48;

        setOrientation(Axis.Y);
    }

    public Meter(String label, ResourceLocation texture) {
        this(Component.literal(label), texture);
    }

    public Meter(Component label, ResourceLocation texture) {
        this(label, label, texture);
    }

    public Meter(String narrationTitle, String label, ResourceLocation texture) {
        this(Component.literal(narrationTitle), Component.literal(label), texture);
    }

    private Component label;
    private ResourceLocation texture;
    private double max = 1;
    private double value = 0.5;
    private double min = 0;
    private Axis orientation = Axis.Y;

    public Axis getOrientation() {
        return orientation;
    }

    public void setOrientation(Axis orientation) {
        this.orientation = orientation;

        if (orientation == Axis.Y) {
            setWidthConstraint(AxisConstraint.fixed(16));
            setHeightConstraint(AxisConstraint.atLeast(font.lineHeight + 18));
        } else {
            setWidthConstraint(AxisConstraint.atLeast(font.lineHeight + 18));
            setHeightConstraint(AxisConstraint.fixed(16));
        }
    }

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

    private float borderSize = 2;
    private int labelMargin = 5;

    @Override
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        updateState();

        if (orientation == Axis.Y)
            renderVertical(pPoseStack, pMouseX, pMouseY, pPartialTick);
        else if (orientation == Axis.X)
            renderHorizontal(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    private void renderVertical(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        var labelAreaHeight = font.lineHeight + labelMargin*2;
        var barHeight = height - labelAreaHeight;

        TorchUtil.textureQuad(
                pPoseStack, texture, x, y, width, borderSize,
                0, 0, 1, borderSize/(float)TEXTURE_HEIGHT
        );
        TorchUtil.textureQuad(
                pPoseStack, texture, x, y + borderSize, width, barHeight - borderSize*2,
                0, borderSize/(float)TEXTURE_HEIGHT,
                1, (TEXTURE_HEIGHT - borderSize)/(float)TEXTURE_HEIGHT
        );
        TorchUtil.textureQuad(
                pPoseStack, texture, x, y + barHeight - borderSize, width, borderSize,
                0, (TEXTURE_HEIGHT - borderSize)/(float)TEXTURE_HEIGHT,
                1, 1
        );

        // Black out the unfilled portion
        TorchUtil.colorQuad(pPoseStack, 0, 0, 0, 1, x, y, width, barHeight - getRenderedValue() * barHeight);

        // Label

        var labelWidth = font.width(label);
        font.draw(pPoseStack, label, x + width / 2 - labelWidth / 2, y + barHeight + labelMargin, 0xFF333333);
    }

    private void renderHorizontal(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

        var labelWidth = font.width(label) + labelMargin*2;
        var barWidth = width - labelWidth;

//        TorchUtil.rotatedTextureQuad(
//                pPoseStack, texture,
//                Color.WHITE,
//                x + labelWidth,
//                y,
//                barWidth,
//                height,
//                0, 0,
//                1, 1,
//                1
//        );

        TorchUtil.rotatedTextureQuad(
                pPoseStack, texture,
                Color.WHITE,
                x + labelWidth,
                y,
                borderSize,
                height,
                0, (TEXTURE_HEIGHT - borderSize)/(float)TEXTURE_HEIGHT,
                1, 1,
                1
        );
        TorchUtil.rotatedTextureQuad(
                pPoseStack, texture,
                Color.WHITE,
                x + labelWidth + borderSize,
                y,
                barWidth - borderSize *2,
                height,
                0, borderSize/(float)TEXTURE_HEIGHT,
                1, (TEXTURE_HEIGHT - borderSize)/(float)TEXTURE_HEIGHT,
                1
        );
        TorchUtil.rotatedTextureQuad(
                pPoseStack, texture,
                Color.WHITE,
                x + labelWidth + barWidth - borderSize,
                y,
                borderSize,
                height,
                0, 0,
                1, borderSize/(float)TEXTURE_HEIGHT,
                1
        );


        var unfilledWidth = barWidth - getRenderedValue() * barWidth;

        // Black out the unfilled portion
        TorchUtil.colorQuad(pPoseStack, 0, 0, 0, 1, x + labelWidth + barWidth - unfilledWidth, y, unfilledWidth, height);

        // Label
        font.draw(pPoseStack, label, x + labelMargin, y + height / 2 - font.lineHeight / 2, 0xFF333333);
    }


}
