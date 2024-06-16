package org.torchmc.ui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.torchmc.ui.TorchUI;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;

/**
 * Item slot widget. Adds the slot texture and can manipulate menu slots to change their position as the widget is moved.
 */
public class SlotWidget extends TorchWidget {
    public static final int SIZE = 18;

    public SlotWidget(Component narrationLabel, Slot slot) {
        super(narrationLabel);

        this.narrationLabel = narrationLabel;
        this.slot = slot;
        this.setX(slot.x);
        this.setY(slot.y);
        this.width = SIZE;
        this.height = SIZE;
    }

    private Slot slot;
    private Component narrationLabel;
    private ResourceLocation texture = TorchUI.builtInTex("gui/widgets/slot.png");
    private Color highlightColor = Color.argb(0x80FFFFFF);

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    @Override
    public AxisConstraint getWidthConstraint(int assumedHeight) {
        return AxisConstraint.fixed(SIZE);
    }

    @Override
    public AxisConstraint getHeightConstraint(int assumedHeight) {
        return AxisConstraint.fixed(SIZE);
    }

    @Override
    public void renderContents(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        var pos = getScreenRect();
        slot.x = pos.getX() - screen.getGuiLeft() + 1;
        slot.y = pos.getY() - screen.getGuiTop() + 1;
        TorchUtil.textureQuad(gfx, texture, getX(), getY(), 18, 18);

        renderSlot(gfx, this.slot);

        if (isHovered()) {
            TorchUtil.colorQuad(gfx, highlightColor, getX() + 1, getY() + 1, width - 2, height - 2);
        }
    }

    @Override
    protected boolean renderTooltip(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        var item = this.slot.getItem();

        if (!item.isEmpty()) {
            gfx.renderTooltip(
                    minecraft.font,
                    Screen.getTooltipFromItem(minecraft, item),
                    item.getTooltipImage(), item,
                    mouseX, mouseY
            );
        }

        return super.renderTooltip(gfx, mouseX, mouseY, partialTick);
    }

    private void renderSlot(GuiGraphics gfx, Slot pSlot) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var itemRenderer = minecraft.getItemRenderer();
        int i = pSlot.x;
        int j = pSlot.y;
        ItemStack itemstack = pSlot.getItem();

        if (itemstack.isEmpty() && pSlot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = pSlot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite sprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                gfx.blit(i, j, 100, 16, 16, sprite);
            }
        }

        RenderSystem.enableDepthTest();
        var screenRect = getScreenRect();
        gfx.renderItem(
                itemstack,
                getX() + 1,
                getY() + 1
        );
        gfx.renderItemDecorations(this.font, itemstack, getX() + 1, getY() + 1);
        //RenderSystem.disableDepthTest(); // TODO this was not matched
    }

    public Slot getSlot() {
        return this.slot;
    }
}
