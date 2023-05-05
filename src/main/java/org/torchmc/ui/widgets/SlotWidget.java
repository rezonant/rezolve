package org.torchmc.ui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
        this.x = slot.x;
        this.y = slot.y;
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
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        var pos = getScreenRect();
        slot.x = pos.getX() - screen.getGuiLeft() + 1;
        slot.y = pos.getY() - screen.getGuiTop() + 1;
        TorchUtil.textureQuad(pPoseStack, texture,  x, y, 18, 18);

        renderSlot(pPoseStack, this.slot);

        if (isHovered()) {
            TorchUtil.colorQuad(pPoseStack, highlightColor, x + 1, y + 1, width - 2, height - 2);
        }

    }


    private void renderSlot(PoseStack pPoseStack, Slot pSlot) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var itemRenderer = minecraft.getItemRenderer();
        int i = pSlot.x;
        int j = pSlot.y;
        ItemStack itemstack = pSlot.getItem();

        this.setBlitOffset(100);
        if (itemstack.isEmpty() && pSlot.isActive()) {
            Pair<ResourceLocation, ResourceLocation> pair = pSlot.getNoItemIcon();
            if (pair != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                blit(pPoseStack, i, j, this.getBlitOffset(), 16, 16, textureatlassprite);
            }
        }

        //RenderSystem.enableDepthTest();

        var screenRect = getScreenRect();
        itemRenderer.blitOffset += 100.0F;
        itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, screenRect.getX() + 1, screenRect.getY() + 1, screenRect.getX() + 1 + (screenRect.getY() + 1) * screen.getXSize());
        itemRenderer.renderGuiItemDecorations(this.font, itemstack, screenRect.getX(), screenRect.getY());
        itemRenderer.blitOffset -= 100.0F;
        this.setBlitOffset(0);

        RenderSystem.disableDepthTest();
    }

    public Slot getSlot() {
        return this.slot;
    }
}
