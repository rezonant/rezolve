package org.torchmc.ui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.torchmc.ui.TorchUI;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;

import java.util.stream.Collectors;

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
            setTooltip((Tooltip) null);
        } else {
            var lines = item.getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);

            setTooltip(
                    Tooltip.create(
                            // TODO: this sucks
                            Component.literal(lines.stream().map(line -> line.getString()).collect(Collectors.joining()))
                    )
            );
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
    protected void renderContents(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderContents(gfx, pMouseX, pMouseY, pPartialTick);
        TorchUtil.textureQuad(gfx, texture, getX(), getY(), 18, 18);

        if (item != null)
            TorchUtil.drawItem(gfx, item, getX() + 1, getY() + 1);

        if (isHovered()) {
            TorchUtil.colorQuad(gfx, highlightColor, getX() + 1, getY() + 1, width - 2, height - 2);
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
