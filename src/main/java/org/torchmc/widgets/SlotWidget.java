package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.torchmc.TorchUI;
import org.torchmc.WidgetBase;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

public class SlotWidget extends WidgetBase {
    public static final int SIZE = 18;

    public SlotWidget(Component narrationLabel, Slot slot) {
        super(narrationLabel);

        this.narrationLabel = narrationLabel;
        this.slot = slot;
        this.x = slot.x;
        this.y = slot.y;
        this.width = SIZE;
        this.height = SIZE;
    }

    private Slot slot;
    private Component narrationLabel;
    private ResourceLocation texture = TorchUI.builtInTex("gui/widgets/slot.png");

    @Override
    public AxisConstraint getDesiredWidth(int assumedHeight) {
        return AxisConstraint.fixed(SIZE);
    }

    @Override
    public AxisConstraint getDesiredHeight(int assumedHeight) {
        return AxisConstraint.fixed(SIZE);
    }

    @Override
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        var pos = getScreenRect();
        slot.x = pos.getX() - screen.getGuiLeft() + 1;
        slot.y = pos.getY() - screen.getGuiTop() + 1;
        TorchUtil.textureQuad(pPoseStack, texture,  x, y, 18, 18);
    }
}
