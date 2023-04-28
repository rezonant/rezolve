package org.torchmc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.torchmc.layout.Panel;
import org.torchmc.util.Color;
import org.torchmc.util.ResizeMode;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

import java.util.function.Consumer;

public class Window extends WidgetBase {
    public Window(Component title) {
        super(title);

        this.title = title;
        this.z = 100;

        setIsDecoration(true);
    }

    public Window(String title) {
        this(Component.literal(title));
    }

    private Component title;
    private boolean resizable = true;
    private boolean movable = true;
    private Panel panel;
    private Size minSize = new Size(50, 50);
    private boolean resizing = false;
    private boolean moving = false;
    private ResizeMode resizeMode;
    private int resizeStartWidth = 0;
    private int resizeStartHeight = 0;
    private double moveStartLeftPos = 0;
    private double moveStartTopPos = 0;

    /**
     * How much margin to put between the edge of the window and its main panel
     */
    protected int panelMargin = 8;

    /**
     * How tall the title bar is
     */
    protected int titlebarHeight = 18;

    /**
     * How wide the drag area is on the window
     */
    protected int dragHandleSize = 4;

    public Component getTitle() {
        return title;
    }

    public void setTitle(Component title) {
        this.title = title;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public <T extends Panel> T setPanel(T panel) {
        return setPanel(panel, p -> {});
    }

    public <T extends Panel> T setPanel(T panel, Consumer<T> initializer) {
        this.panel = addChild(panel, initializer);
        applyDimensions();
        return panel;
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return x < pMouseX && pMouseX < x + width + dragHandleSize / 2 && y < pMouseY && pMouseY < y + height + dragHandleSize / 2;
    }

    protected void renderTitleBar(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var border = 2;

        if (movable) {
            TorchUtil.colorQuad(
                    poseStack, 0xFF999999,
                    x + border, y + border,
                    width - border * 2, titlebarHeight - border * 2
            );

            if (getTitle() != null)
                font.draw(poseStack, getTitle(), x + 8, y + 6, 4210752);
        }
    }

    @Override
    protected void screenScissor(Runnable runnable) {
        runnable.run();
    }

    @Override
    protected void renderBackground(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        TorchUtil.insetBox(
                pPoseStack,
                TorchUI.builtInTex("gui/widgets/screen_background.png"),
                x, y, width, height
        );

        renderTitleBar(pPoseStack, pPartialTick, pMouseX, pMouseY);
    }

    protected void renderContents(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

//        if (panel != null)
//            panel.render(poseStack, mouseX, mouseY, partialTick);

        if (!resizing && resizable) {
            if (hoveringRightEdge(mouseX, mouseY) && hoveringBottomEdge(mouseX, mouseY)) {
                TorchUtil.colorQuad(
                        poseStack, Color.WHITE.withAlpha(0.5f),
                        getRightEdgeStart(), getBottomEdgeStart(),
                        dragHandleSize, dragHandleSize
                );
            } else if (hoveringRightEdge(mouseX, mouseY)) {
                TorchUtil.colorQuad(
                        poseStack, Color.WHITE.withAlpha(0.5f),
                        getRightEdgeStart(), y,
                        dragHandleSize, height
                );
            } else if (hoveringBottomEdge(mouseX, mouseY)) {
                TorchUtil.colorQuad(
                        poseStack, Color.WHITE.withAlpha(0.5f),
                        x, getBottomEdgeStart(),
                        width, dragHandleSize
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        clickX = pMouseX;
        clickY = pMouseY;

        if (screen instanceof TorchScreen<?> torchScreen) {
            torchScreen.sendWindowToTop(this);
        }

        if (movable) {
            if (hoveringMenuBar(pMouseX, pMouseY)) {
                moving = true;
                moveStartLeftPos = x;
                moveStartTopPos = y;



                return true;
            }
        }

        if (resizable) {
            if (hoveringBottomEdge(pMouseX, pMouseY) && hoveringRightEdge(pMouseX, pMouseY)) {
                resizing = true;
                resizeMode = ResizeMode.BOTTOM_RIGHT;
                resizeStartWidth = width;
                resizeStartHeight = height;
                return true;
            } else if (hoveringBottomEdge(pMouseX, pMouseY)) {
                resizing = true;
                resizeMode = ResizeMode.BOTTOM;
                resizeStartWidth = width;
                resizeStartHeight = height;
                return true;
            } else if (hoveringRightEdge(pMouseX, pMouseY)) {
                resizing = true;
                resizeMode = ResizeMode.RIGHT;
                resizeStartWidth = width;
                resizeStartHeight = height;
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

        if (resizing) {
            int newWidth = width;
            int newHeight = height;

            switch (resizeMode) {
                case RIGHT -> {
                    newWidth = (int)(resizeStartWidth + (pMouseX - clickX));
                }
                case BOTTOM -> {
                    newHeight = (int)(resizeStartHeight + (pMouseY - clickY));
                }
                case BOTTOM_RIGHT -> {
                    newWidth = (int)(resizeStartWidth + (pMouseX - clickX));
                    newHeight = (int)(resizeStartHeight + (pMouseY - clickY));
                }
            }

            newWidth = Math.max(minSize.width, newWidth);
            newHeight = Math.max(minSize.height, newHeight);

            resize(newWidth, newHeight);
            wasResizedByUser(newWidth, newHeight);

            return true;
        } else if (moving) {
            int newX = (int)(moveStartLeftPos + (pMouseX - clickX));
            int newY = (int)(moveStartTopPos + (pMouseY - clickY));

            move(newX, newY);
            wasMovedByUser(newX, newY);

            return true;
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    protected void wasMovedByUser(int x, int y) {

    }

    protected void wasResizedByUser(int x, int y) {

    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        resizing = false;
        moving = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private boolean hoveringMenuBar(double pMouseX, double pMouseY) {
        return x < pMouseX && pMouseX < x + width
                && y < pMouseY && pMouseY < y + titlebarHeight;
    }

    private boolean hoveringBottomEdge(double pMouseX, double pMouseY) {
        return getBottomEdgeStart() < pMouseY && pMouseY < getBottomEdgeStart() + dragHandleSize;
    }

    private int getRightEdgeStart() {
        return x + width - dragHandleSize / 2;
    }

    private boolean hoveringRightEdge(double mouseX, double mouseY) {
        return getRightEdgeStart() < mouseX && mouseX < getRightEdgeStart() + dragHandleSize;
    }

    private int getBottomEdgeStart() {
        return y + height - dragHandleSize / 2;
    }

    protected void applyDimensions() {
        if (panel != null) {
            panel.move(
                    panelMargin,
                    panelMargin + font.lineHeight + 2,
                    width - panelMargin * 2,
                    height - panelMargin * 2 - font.lineHeight - 2
            );
        }
    }

    @Override
    protected void didResize() {
        applyDimensions();
    }
}
