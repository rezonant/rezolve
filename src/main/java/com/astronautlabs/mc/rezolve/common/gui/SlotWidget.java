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
import net.minecraft.world.inventory.Slot;

public class SlotWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public SlotWidget(int screenX, int screenY, Component narrationLabel, Slot slot) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.narrationLabel = narrationLabel;
        this.slot = slot;
    }

    private int screenX;
    private int screenY;
    private Slot slot;
    private Component narrationLabel;
    private ResourceLocation texture = new ResourceLocation("rezolve", "textures/gui/widgets/slot.png");

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RezolveGuiUtil.textureQuad(pPoseStack, texture, screenX + slot.x - 1, screenY + slot.y - 1, 18, 18);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, narrationLabel);
    }
}
