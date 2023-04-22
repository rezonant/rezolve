package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.torchmc.TorchUI;
import org.torchmc.WidgetBase;
import org.torchmc.util.Color;
import org.torchmc.util.TorchUtil;

public class ProgressIndicator extends WidgetBase {
    public static final ResourceLocation ARROW_TEXTURE = TorchUI.builtInTex("textures/gui/widgets/arrow.png");

    public ProgressIndicator(int x, int y, Component narrationTitle) {
        super(narrationTitle);
    }

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
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        updateState();

        double size = 32;
        double width = size * value;

        TorchUtil.textureQuad(
                pPoseStack, ARROW_TEXTURE,
                Color.of(0.6, 0.6, 0.6, 1),
                x, y, size, size, 0, 0, 1, 1
        );

        TorchUtil.textureQuad(
                pPoseStack, ARROW_TEXTURE,
                Color.of(1, 0, 0, 1),
                x, y, width, size, 0, 0, (float)value, 1
        );
    }
}
