package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.torchmc.TorchUI;
import org.torchmc.WidgetBase;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

public class SlotWidget extends WidgetBase {
    public SlotWidget(Component narrationLabel, Slot slot) {
        super(narrationLabel);

        this.narrationLabel = narrationLabel;
        this.slot = slot;
        this.x = slot.x;
        this.y = slot.y;
        this.width = 18;
        this.height = 18;

        setDesiredSize(new Size(18, 18));
    }

    private Slot slot;
    private Component narrationLabel;
    private ResourceLocation texture = TorchUI.builtInTex("gui/widgets/slot.png");

    @Override
    protected void didMove() {
        super.didMove();

        var pos = getScreenRect();
        slot.x = screen.getGuiLeft() + pos.getX();
        slot.y = screen.getGuiTop() + pos.getY();
    }

    @Override
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        TorchUtil.textureQuad(pPoseStack, texture, screen.getGuiLeft() + slot.x - 1, screen.getGuiTop() + slot.y - 1, 18, 18);
    }
}
