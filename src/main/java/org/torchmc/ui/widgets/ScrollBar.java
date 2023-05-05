package org.torchmc.ui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;

/**
 * A scrollbar which can be composed into another view to add support for scrolling to any Torch widget.
 */
public class ScrollBar extends TorchWidget {
    public ScrollBar() {
        super(Component.literal(""));
    }

    private int scrollPosition = 0;
    private int contentHeight = 0;

    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
    }

    public void scrollPositionChanged(int position) {

    }

    private boolean buttonDragged = false;
    private double dragMouseY = 0;
    private int dragPosition = 0;
    private Color thumbColor = Color.argb(0xFF666666);
    private Color hoverColor = Color.argb(0xFF777777);
    private Color dragColor = Color.argb(0xFF555555);
    private Color barColor = Color.argb(0xFFAAAAAA);

    public Color getThumbColor() {
        return thumbColor;
    }

    public void setThumbColor(Color thumbColor) {
        this.thumbColor = thumbColor;
    }

    public Color getHoverColor() {
        return hoverColor;
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
    }

    public Color getDragColor() {
        return dragColor;
    }

    public void setDragColor(Color dragColor) {
        this.dragColor = dragColor;
    }

    public boolean isThumbHovered(double pMouseX, double pMouseY) {
        return x < pMouseX && pMouseX < x + width
                && y + getThumbPosition() < pMouseY && pMouseY < y + getThumbPosition() + getThumbSize()
                ;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (isThumbHovered(pMouseX, pMouseY)) {
            buttonDragged = true;
            dragMouseY = pMouseY;
            dragPosition = scrollPosition;

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        buttonDragged = false;
        return true;
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
    }

    public int getThumbSize() {
        return Math.min(height, (int)((height / (double)contentHeight) * height));
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

        int scrollBarSpace = height - getThumbSize();
        scrollPosition = dragPosition + (int)((pMouseY - dragMouseY) / (double)scrollBarSpace * getMaxScrollPosition());
        scrollPosition = Math.max(0, Math.min(scrollPosition, getMaxScrollPosition()));
        scrollPositionChanged(scrollPosition);
        return true;
    }

    public int getMaxScrollPosition() {
        return contentHeight - height;
    }

    public int getThumbPosition() {
        int scrollBarSpace = height - getThumbSize();
        return (int)((scrollPosition / (double)getMaxScrollPosition()) * scrollBarSpace);
    }

    @Override
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

        if (getMaxScrollPosition() > 0) {
            Color color = thumbColor;

            if (buttonDragged)
                color = dragColor;
            else if (isThumbHovered(pMouseX, pMouseY))
                color = hoverColor;

            TorchUtil.colorQuad(pPoseStack, barColor, x, y, width, height);
            TorchUtil.colorQuad(pPoseStack, color.argb(), x, y + getThumbPosition(), width, getThumbSize());
        }
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        // No op
    }
}
