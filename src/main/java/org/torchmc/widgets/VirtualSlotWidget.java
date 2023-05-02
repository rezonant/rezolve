package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.torchmc.TorchUI;
import org.torchmc.TorchWidget;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Color;
import org.torchmc.util.TorchUtil;

public class VirtualSlotWidget extends TorchWidget {
    public static final int SIZE = 18;

    public VirtualSlotWidget(Component narrationTitle) {
        super(narrationTitle);

        resize(SIZE);

        setWidthConstraint(AxisConstraint.fixed(SIZE));
        setHeightConstraint(AxisConstraint.fixed(SIZE));
    }

    private ResourceLocation texture = TorchUI.builtInTex("gui/widgets/slot.png");
    private ItemStack item;
    private Color highlightColor = Color.argb(0x80FFFFFF);
    private Runnable handler;

    public void setHandler(Runnable handler) {
        this.handler = handler;
    }

    public Runnable getHandler() {
        return handler;
    }

    public void setItem(ItemStack item) {
        this.item = item;
        if (item == null) {
            setTooltip((Component) null);
        } else {
            setTooltip(item.getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL));
        }
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public int getConstrainedWidth(int height) {
        return SIZE;
    }

    @Override
    public int getConstrainedHeight(int width) {
        return SIZE;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    @Override
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
        TorchUtil.textureQuad(pPoseStack, texture,  x, y, 18, 18);

        if (item != null)
            TorchUtil.drawItem(pPoseStack, item, x + 1, y + 1);

        if (isHovered()) {
            TorchUtil.colorQuad(pPoseStack, highlightColor, x + 1, y + 1, width - 2, height - 2);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return true;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (this.handler != null) {
            this.handler.run();
            return true;
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }
}
