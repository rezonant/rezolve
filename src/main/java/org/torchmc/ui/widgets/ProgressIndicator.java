package org.torchmc.ui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.torchmc.ui.TorchUI;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;

/**
 * Renders as an arrow, and can show progress by rendering a portion of the arrow as red/gray.
 */
public class ProgressIndicator extends TorchWidget {
    public static final ResourceLocation ARROW_TEXTURE = TorchUI.builtInTex("gui/widgets/arrow.png");

    public ProgressIndicator(Component narrationTitle, int size) {
        super(narrationTitle);

        this.size = size;
        setWidthConstraint(AxisConstraint.fixed(size));
        setHeightConstraint(AxisConstraint.fixed(size));
    }

    public ProgressIndicator(Component narrationTitle) {
        this(narrationTitle, 32);
    }

    public ProgressIndicator() {
        this(Component.empty(), 32);
    }

    public ProgressIndicator(int size) {
        this(Component.empty(), size);
    }

    int size = 32;

    @Override
    public int getConstrainedWidth(int height) {
        return size;
    }

    @Override
    public int getConstrainedHeight(int width) {
        return size;
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
