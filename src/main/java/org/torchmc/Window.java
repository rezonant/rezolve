package org.torchmc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.Rezolve;
import net.minecraft.network.chat.Component;
import org.torchmc.layout.AxisAlignment;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.Panel;
import org.torchmc.util.Color;
import org.torchmc.util.ResizeMode;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;
import org.torchmc.widgets.IconButton;
import org.torchmc.widgets.Label;
import org.torchmc.widgets.Spacer;

import java.util.function.Consumer;

/**
 * Represents a floating window rendered as part of a TorchScreen. Windows are movable, resizable, and support
 * Z positioning. Though intended to be used primarily with TorchScreen, Windows can also be added to other kinds of
 * Screen, though not all features are supported. Notably, Z ordering is not supported in such a configuration.
 */
public class Window extends TorchWidget {
    public Window(Component title) {
        super(title);

        this.title = title;

        setIsDecoration(true);

        titlebarPanel = addChild(new HorizontalLayoutPanel(), titlebarRoot -> {
            titlebarRoot.setAlignment(AxisAlignment.CENTER);
            titlebarRoot.addChild(new Label(title), label -> {
                label.setColor(Color.argb(0xFF404040));
                label.setTopPadding(1);
                label.setLeftPadding(5);
                titlebarLabel = label;
            });
            titlebarRoot.addChild(new Spacer());
            titlebarRoot.addChild(new IconButton(Rezolve.tr("screens.rezolve.close"), Rezolve.icon("x"), titlebarHeight - 7), btn -> {
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setHandler(() -> onClose());

                btn.setVisible(closable);
                closeButton = btn;
            });
        });

        titlebarPanel.setVisible(titleBarVisible);
    }

    public Window(String title) {
        this(Component.literal(title));
    }

    private Panel titlebarPanel;
    private Label titlebarLabel;
    private IconButton closeButton;
    private Component title;
    private boolean resizable = true;
    private boolean movable = true;
    private boolean closable = true;
    private boolean titleBarVisible = true;
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

    /**
     * Set the current title for this window (as shown in the titlebar).
     * @param title
     */
    public void setTitle(Component title) {
        this.title = title;
        this.titlebarLabel.setContent(title);
    }

    public boolean isResizable() {
        return resizable;
    }

    /**
     * Change whether this window is resizable by the user.
     * @param resizable
     */
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isClosable() {
        return closable;
    }

    public boolean isTitleBarVisible() {
        return titleBarVisible;
    }

    /**
     * Change whether this window should render its titlebar. The height of the window will be automatically adjusted
     * when the titlebar's visibility changes.
     *
     * @param titleBarVisible
     */
    public void setTitleBarVisible(boolean titleBarVisible) {
        if (this.titleBarVisible == titleBarVisible)
            return;

        this.titleBarVisible = titleBarVisible;
        this.titlebarPanel.setVisible(titleBarVisible);

        if (titleBarVisible) {
            resize(width, height + titlebarHeight);
        } else {
            resize(width, height - titlebarHeight);
        }

        applyDimensions();
    }

    /**
     * Change whether this window is closable.
     * @param closable
     */
    public void setClosable(boolean closable) {
        this.closable = closable;
        if (closeButton != null)
            closeButton.setVisible(closable);
    }

    /**
     * Set the primary panel of this window. The primary panel will be automatically positioned to take up the entire
     * space of the window.
     * @param panel
     * @return
     * @param <T>
     */
    public <T extends Panel> T setPanel(T panel) {
        return setPanel(panel, p -> {});
    }

    /**
     * Set the primary panel of this window and run the given initializer against it. The primary panel will be
     * automatically positioned to take up the entire space of the window.
     *
     * @param panel
     * @param initializer
     * @return
     * @param <T>
     */
    public <T extends Panel> T setPanel(T panel, Consumer<T> initializer) {
        this.panel = addChild(panel, initializer);
        applyDimensions();
        return panel;
    }

    /**
     * Check if the given mouse coordinates fall within the bounding box of this window.
     * @param pMouseX The X coordinate within the rectangle of this widget's parent widget (typically the Screen)
     * @param pMouseY The Y coordinate within the rectangle of this widget's parent widget (typically the Screen)
     *
     * @return
     */
    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return x < pMouseX && pMouseX < x + width + dragHandleSize / 2 && y < pMouseY && pMouseY < y + height + dragHandleSize / 2;
    }

    /**
     * Renders the window's title bar. Can be overridden to customize. Note that the titlebar contents itself are
     * rendered using Torch's layout system and widgets, so the default implementation of this method only draws the
     * colored rectangle background.
     * @param poseStack
     * @param partialTick
     * @param mouseX
     * @param mouseY
     */
    protected void renderTitleBar(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var border = 2;

        if (movable && titleBarVisible) {
            TorchUtil.colorQuad(
                    poseStack, 0xFF999999,
                    x + border, y + border,
                    width - border * 2, titlebarHeight - border * 2
            );
        }
    }

    public boolean isMovable() {
        return movable;
    }

    /**
     * Change whether this window is movable by the user.
     * @param movable
     */
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public void onClose() {
        setVisible(false);
    }

    /**
     * Window's implementation of screenScissor() is effectively a no-op since windows are not constrained to the
     * bounding box of the Screen which contains it.
     * @param runnable
     */
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
        if (screen instanceof TorchScreen<?> torchScreen) {
            torchScreen.sendWindowToTop(this);
        }

        if (super.mouseClicked(pMouseX, pMouseY, pButton))
            return true;

        clickX = pMouseX;
        clickY = pMouseY;

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

        return false;
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

    /**
     * Called when the user has moved the window.
     * @param x
     * @param y
     */
    protected void wasMovedByUser(int x, int y) {

    }

    /**
     * Called when the user has resized the window.
     * @param x
     * @param y
     */
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
        return getBottomEdgeStart() < pMouseY && pMouseY < getBottomEdgeStart() + dragHandleSize && x < pMouseX && pMouseX < x + width + dragHandleSize / 2;
    }

    private int getRightEdgeStart() {
        return x + width - dragHandleSize / 2;
    }

    private boolean hoveringRightEdge(double mouseX, double mouseY) {
        return getRightEdgeStart() < mouseX && mouseX < getRightEdgeStart() + dragHandleSize && y < mouseY && mouseY < y + height + dragHandleSize / 2;
    }

    private int getBottomEdgeStart() {
        return y + height - dragHandleSize / 2;
    }

    protected void applyDimensions() {
        int border = 3;
        titlebarPanel.move(border, border, width - border*2, titlebarHeight - border*2);

        int effectiveTitlebarHeight = titleBarVisible ? titlebarHeight : 0;

        if (panel != null) {
            panel.move(
                    panelMargin,
                    effectiveTitlebarHeight + panelMargin,
                    width - panelMargin * 2,
                    height - panelMargin * 2 - effectiveTitlebarHeight
            );
        }
    }

    @Override
    protected void didResize() {
        applyDimensions();
    }
}
