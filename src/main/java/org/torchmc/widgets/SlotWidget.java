package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.torchmc.TorchUI;
import org.torchmc.WidgetBase;
import org.torchmc.util.TorchUtil;

public class SlotWidget extends WidgetBase {
    public SlotWidget(int screenX, int screenY, Component narrationLabel, Slot slot) {
        super(narrationLabel);

        this.screenX = screenX;
        this.screenY = screenY;
        this.narrationLabel = narrationLabel;
        this.slot = slot;
    }

    private int screenX;
    private int screenY;
    private Slot slot;
    private Component narrationLabel;
    private ResourceLocation texture = TorchUI.builtInTex("gui/widgets/slot.png");

    @Override
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        TorchUtil.textureQuad(pPoseStack, texture, screenX + slot.x - 1, screenY + slot.y - 1, 18, 18);
    }
}
