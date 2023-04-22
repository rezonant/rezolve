package org.torchmc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class WidgetBase extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    WidgetBase(Component narrationTitle, int x, int y, int width, int height) {
        this.narrationTitle = narrationTitle;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> acScreen)
            this.screen = acScreen;
    }

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    private boolean visible = true;
    private WidgetBase parent;
    private AbstractContainerScreen screen;
    private boolean isDecoration = false;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rect2i getScreenRect() {
        var x = 0;
        var y = 0;
        var self = this;
        while (self != null) {
            x += self.x;
            y += self.y;
            self = self.parent;
        }

        return new Rect2i(x, y, width, height);
    }

    public Rect2i getScreenDesiredRect() {
        var x = 0;
        var y = 0;
        var self = this;
        while (self != null) {
            x += self.x;
            y += self.y;
            self = self.parent;
        }

        var size = getDesiredSize();
        return new Rect2i(x, y, size.width, size.height);
    }

    /**
     * True if this widget is part of a decoration that sits outside of the bounds of the containing screen.
     * This disables the built-in content clipping.
     * @return
     */
    public boolean isDecoration() {
        if (isDecoration)
            return true;
        if (parent != null)
            return parent.isDecoration();

        return false;
    }

    public void setIsDecoration(boolean decoration) {
        isDecoration = decoration;
    }

    public final void move(int x, int y) {
        if (this.x == x && this.y == y)
            return;

        this.x = x;
        this.y = y;
        didMove();
    }

    protected void didMove() {

    }

    protected void didResize() {

    }

    protected List<WidgetBase> children = new ArrayList<>();

    void adoptParent(WidgetBase parent) {
        this.parent = parent;
    }

    /**
     * For widgets which have no WidgetBase parent (for instance top level Panels adopted by Screens),
     * this allows you to convey the size of the parent without assigning it as a parent (since you wouldn't be
     * able to, as the Screen does not extend from WidgetBase). You don't need to use this directly.
     * @param size
     */
    public void setParentSize(Size size) {
        if (this.parent != null)
            throw new RuntimeException("You cannot set the parent size on a widget which is already parented.");
    }

    public <T extends WidgetBase> T addChild(T widget) {
        children.add(widget);
        widget.adoptParent(this);
        return widget;
    }

    public WidgetBase[] getChildren() {
        return children.toArray(new WidgetBase[children.size()]);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return x < pMouseX && pMouseX < x + width && y < pMouseY && pMouseY < y + height;
    }

    private boolean hovered = false;
    private boolean focused = false;
    private boolean active = true;

    public boolean isHoveredOrFocused() {
        return hovered || focused;
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean isFocused() {
        return focused;
    }

    /**
     * True if this widget can be interacted with and focused.
     * @return
     */
    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Called when the focus state has *changed*. This does NOT mean we are focused! This means that the user's current focus
     * has changed, and we are somehow affected by it. If we were already focused, then we are no longer focused, and
     * if we were not focused, then we are now focused. Please do not call this manually to set focus! Kittens die when
     * you do that.
     *
     * @param direction
     */
    @Override
    public boolean changeFocus(boolean direction) {
        focused = !focused;

        if (active && visible) {
            focused = !focused;
            focusStateChanged(focused);
            if (focused)
                becameFocused();
            else
                becameUnfocused();

            return focused;
        } else {
            return false;
        }
    }

    public void focusStateChanged(boolean focused) {

    }

    public void becameFocused() {

    }

    public void becameUnfocused() {

    }

    public void setVisible(boolean visible) {
        if (this.visible == visible)
            return;

        this.visible = visible;

        if (visible)
            didBecomeVisible();
        else
            didBecomeInvisible();
    }

    public boolean isVisible() {
        return visible;
    }

    protected void didBecomeVisible() {

    }

    protected void didBecomeInvisible() {

    }

    public final void move(int x, int y, int width, int height) {
        if (this.x == x && this.y == y && this.width == width && this.height == height)
            return;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        didMove();
        didResize();
    }

    public final void resize(int width, int height) {
        this.width = width;
        this.height = height;
        didResize();
    }

    Component narrationTitle;

    @Override
    public final void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!visible)
            return;

        hovered = isMouseOver(pMouseX, pMouseY);

        screenScissor(() -> {
            pushPose(pPoseStack, () -> {
                repose(pPoseStack, () -> pPoseStack.translate(x, y, 0));
                renderChildren(pPoseStack, pMouseX - x, pMouseY - y, pPartialTick);
            });
            renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
        });

    }

    protected void renderChildren(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        for (var child : children) {
            child.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, narrationTitle);
    }

    protected void screenScissor(Runnable runnable) {
        var border = 2;
        if (screen != null && !isDecoration())
            displayScissor(
                    screen.getGuiLeft() + border, screen.getGuiTop() + border,
                    screen.getXSize() - border * 2, screen.getYSize() - border * 2,
                    runnable);
        else
            runnable.run();
    }

    protected void scissor(PoseStack stack, int x, int y, int width, int height, Runnable runnable) {
        var tr = RezolveGuiUtil.getTranslation(stack.last().pose());

        enableScissor(
                (int)tr.x() + x,
                (int)tr.y() + y,
                (int)tr.x() + x + width,
                (int)tr.y() + y + height
        );

        try {
            runnable.run();
        } finally {
            disableScissor();
        }
    }

    protected void displayScissor(int x, int y, int width, int height, Runnable runnable) {
        enableScissor(x, y, x + width, y + height);
        try {
            runnable.run();
        } finally {
            disableScissor();
        }
    }

    protected void pushPose(PoseStack stack, Runnable runnable) {
        stack.pushPose();
        try {
            runnable.run();
        } finally {
            stack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    protected void repose(PoseStack stack, Runnable runnable) {
        runnable.run();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        pMouseX -= x;
        pMouseY -= y;

        for (var child : children) {
            if (child.isMouseOver(pMouseX, pMouseY)) {
                if (child.mouseScrolled(pMouseX, pMouseY, pDelta)) {
                    return true;
                }
            }
        }
        return GuiEventListener.super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        pMouseX -= x;
        pMouseY -= y;

        for (var child : children) {
            if (child.isMouseOver(pMouseX, pMouseY)) {
                if (child.mouseClicked(pMouseX, pMouseY, pButton)) {
                    draggedWidget = child;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {        pMouseX -= x;
        pMouseY -= y;

        if (draggedWidget != null && draggedWidget instanceof GuiEventListener listener) {
            listener.mouseReleased(pMouseX, pMouseY, pButton);
        }

        draggedWidget = null;

        return false;
    }

    private Widget draggedWidget = null;

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        pMouseX -= x;
        pMouseY -= y;

        if (draggedWidget != null && draggedWidget instanceof GuiEventListener listener) {
            listener.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return false;
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        pMouseY -= y;

        for (var child : children) {
            if (child.isMouseOver(pMouseX, pMouseY)) {
                child.mouseMoved(pMouseX, pMouseY);
            }
        }
    }

    /**
     * Determines what size this widget would *like* to have. This is used
     * as a feedback mechanism for panels which can use it, such as the Vertical/HorizontalLayoutPanel
     *
     * @return
     */
    public Size getDesiredSize() {
        return null;
    }

    private Size growScale;

    public void setGrowScale(int growScale) {
        this.growScale = new Size(growScale, growScale);
    }

    public void setGrowScale(Size growScale) {
        this.growScale = growScale;
    }

    public Size getGrowScale() {
        if (growScale == null)
            return null;

        return growScale.copy();
    }
}
