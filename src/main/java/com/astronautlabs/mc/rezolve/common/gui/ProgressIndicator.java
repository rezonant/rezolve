package com.astronautlabs.mc.rezolve.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ProgressIndicator extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("rezolve:textures/gui/widgets/arrow.png");

    public ProgressIndicator(int x, int y, Component narrationTitle) {
        this.narrationTitle = narrationTitle;
        this.x = x;
        this.y = y;
    }

    private Component narrationTitle;
    private int x;
    private int y;
    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    protected void updateState() {

    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        updateState();

        double size = 32;
        double width = size * value;

        RezolveGuiUtil.textureQuad(
                pPoseStack, ARROW_TEXTURE,
                Color.of(0.6, 0.6, 0.6, 1),
                x, y, size, size, 0, 0, 1, 1
        );

        RezolveGuiUtil.textureQuad(
                pPoseStack, ARROW_TEXTURE,
                Color.of(1, 0, 0, 1),
                x, y, width, size, 0, 0, (float)value, 1
        );
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, narrationTitle);
    }
}
