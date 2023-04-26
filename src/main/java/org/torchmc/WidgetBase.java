package org.torchmc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.util.RezolveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.torchmc.layout.Axis;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class WidgetBase extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public WidgetBase(Component narrationTitle) {
        this.narrationTitle = narrationTitle;

        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> acScreen)
            this.screen = acScreen;

        minecraft = Minecraft.getInstance();
        font = minecraft.font;
    }

    private boolean visible = true;
    private WidgetBase parent;
    protected AbstractContainerScreen screen;
    private boolean isDecoration = false;
    private boolean hovered = false;
    private boolean focused = false;
    private boolean active = true;
    private List<Component> tooltip = null;

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Minecraft minecraft;
    protected Font font;

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

    public List<Component> getTooltip() {
        return tooltip;
    }

    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = List.of(tooltip);
    }

    public void setTooltip(String text) {
        this.tooltip = List.of(Component.literal(text));
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
        return addChild(widget, w -> {});
    }

    public <T extends WidgetBase> T addChild(T widget, Consumer<T> initializer) {
        children.add(widget);
        widget.adoptParent(this);
        widget.runInitializer(() -> initializer.accept(widget));

        if (parent != null)
            parent.hierarchyDidChange();

        return widget;
    }

    private boolean hierarchyChangesHeld = false;
    private boolean hierarchyIsChanging = false;

    /**
     * Runs the given runnable while pausing hierarchy updates. If hierarchy updates are already paused,
     * this runs the function without modifying the holding state.
     * @param runnable
     */
    public void runInitializer(Runnable runnable) {
        boolean didHoldChanges = false;
        boolean wasChanged = false;

        if (!hierarchyChangesHeld) {
            hierarchyChangesHeld = true;
            hierarchyIsChanging = false;
            didHoldChanges = true;
        }

        try {
            runnable.run();
        } finally {
            if (didHoldChanges) {
                wasChanged = hierarchyIsChanging;
                hierarchyChangesHeld = false;
                hierarchyIsChanging = false;
            }
        }

        if (wasChanged && parent != null)
            parent.hierarchyDidChange();
    }

    /**
     * Notify parents that the desiredSize() has changed in case they are interested.
     * Note: Should only be called if you have overridden getCustomSize() to include custom on-demand logic.
     * If you are using setDesiredSize() without any custom getDesiredSize() implementation, you should not
     * call this, as it calls this already.
     */
    protected void desiredSizeDidChange() {
        if (parent != null)
            parent.hierarchyDidChange();
    }

    /**
     * Fired when a widget has been added or removed somewhere below this widget, or when such a widget
     * has changed its desired size.
     */
    protected void hierarchyDidChange() {
        if (hierarchyChangesHeld) {
            hierarchyIsChanging = true;
        } else {
            if (parent != null)
                parent.hierarchyDidChange();
        }
    }

    public WidgetBase[] getChildren() {
        return children.toArray(new WidgetBase[children.size()]);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return x < pMouseX && pMouseX < x + width && y < pMouseY && pMouseY < y + height;
    }

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

    private void setFocusState(boolean focused) {
        if (this.focused == focused)
            return;

        this.focused = focused;
        focusStateChanged(focused);
        if (focused)
            becameFocused();
        else
            becameUnfocused();
    }

    private WidgetBase focusedChild;
    private boolean focusable = false;

    /**
     * True if this widget is itself focusable. Note that this is distinct from the widget being able to *handle* focus
     * amongst its children, all widgets can do that.
     * @return
     */
    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    /**
     * Called when the focus state has *changed*. This does NOT mean we are focused! This means that the user's current focus
     * has changed, and we are somehow affected by it. If we were already focused, then we are no longer focused, and
     * if we were not focused, then we are now focused. Please do not call this manually to set focus! Kittens die when
     * you do that.
     *
     * If the widget has multiple focusable elements, then it should try to find the next focusable one, based on direction
     * (true for forward, false for backward). If the end of the set of focusable elements is reached, this method should
     * set the current focused widget to null and return false. The screen is then responsible for restarting the focus
     * chain so that focus wraps around.
     *
     * If this widget has no currently focused element, then the first focusable element should become focused when direction
     * is true, and the last focusable element should become focused when direction is false.
     *
     * @param direction
     */
    @Override
    public boolean changeFocus(boolean direction) {
        if (active && visible) {

            if (focusable && focusedChild == null) {
                setFocusState(!focused);
                if (focused) // we have just become focused, so we can stop here
                    return true;
            }

            var list = direction ? children : RezolveUtil.reverseList(children);
            int index = focusedChild != null ? list.indexOf(focusedChild) : 0;

            for (int i = index; i < list.size(); ++i) {
                var child = list.get(i);

                if (child.changeFocus(direction)) {
                    // The child has a new focusable widget, so nothing else to do
                    if (focusedChild != null) {
                        focusedChild.setFocusState(false);
                    }
                    focusedChild = child;
                    return true;
                }
            }

            if (focusedChild != null) {
                focusedChild.setFocusState(false);
            }
            focusedChild = null;
            return false;
        }

        return false;
    }

    /**
     * Make this widget take the current focus.
     */
    public void takeFocus() {
        if (isFocused())
            return;

        var parent = this.parent;
        var child = this;

        while (parent != null) {
            parent.setFocusState(false);
            for (var otherChild : parent.children) {
                if (otherChild == child)
                    continue;
                otherChild.clearFocus();
            }

            parent.focusedChild = child;

            // Next round

            child = parent;
            parent = parent.parent;
        }

        for (var renderable : screen.renderables) {
            if (renderable == child)
                continue;

            if (renderable instanceof WidgetBase widget) {
                widget.clearFocus();
            }
        }

        screen.setFocused(child);

        setFocusState(true);
    }

    private void clearFocus() {
        setFocusState(false);
        focusedChild = null;
        for (var child : children) {
            child.clearFocus();
        }
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (focusedChild != null)
            return focusedChild.charTyped(pCodePoint, pModifiers);
        return false;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (focusedChild != null)
            return focusedChild.keyPressed(pKeyCode, pScanCode, pModifiers);
        return false;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (focusedChild != null)
            return focusedChild.keyReleased(pKeyCode, pScanCode, pModifiers);
        return false;
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

        // Since hierarchy change notifications are used to inform relayouting, it's important
        // to send hierarchy change notifications when visibility changes to enable layouts to
        // update as appropriate.
        if (parent != null)
            hierarchyDidChange();
    }

    public boolean isVisible() {
        return visible;
    }

    public void didBecomeVisible() {

    }

    public void didBecomeInvisible() {

    }

    public final void move(int x, int y, int width, int height) {
        move(x, y);
        resize(width, height);
    }

    public final void resize(int width, int height) {
        if (this.width == width && this.height == height)
            return;

        this.width = width;
        this.height = height;
        didResize();
    }

    protected Component narrationTitle;

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

        if (isHovered())
            renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        var tooltip = getTooltip(); // Important to allow customization of tooltip behavior in widget classes
        if (tooltip != null && tooltip.size() > 0)
            screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
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

    /**
     * Control whether scissor operations are used globally. Useful for debugging issues with coordinates, where something appears
     * not to render, but is actually rendered outside of scissor bounds due to coordinate issues.
     */
    public static boolean enableScissoring = true;

    protected void scissor(PoseStack stack, int x, int y, int width, int height, Runnable runnable) {
        var tr = TorchUtil.getTranslation(stack.last().pose());

        if (enableScissoring) {
            enableScissor(
                    (int) tr.x() + x,
                    (int) tr.y() + y,
                    (int) tr.x() + x + Math.max(1, width),
                    (int) tr.y() + y + Math.max(1, height)
            );
        }

        try {
            runnable.run();
        } finally {
            if (enableScissoring)
                disableScissor();
        }
    }

    protected void displayScissor(int x, int y, int width, int height, Runnable runnable) {
        if (enableScissoring) {
            enableScissor(x, y, x + width, y + height);
        }

        try {
            runnable.run();
        } finally {
            if (enableScissoring)
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

    protected boolean mouseDown;
    protected double clickX;
    protected double clickY;

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        mouseDown = true;
        clickX = pMouseX;
        clickY = pMouseY;

        // Pass to children, putting it into their parent coordinate space (ours)

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
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        mouseDown = false;

        // Pass to children, putting it into their parent coordinate space (ours)

        pMouseX -= x;
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

    Size desiredSize = null;

    /**
     * Set the size this widget desires to be. Used as an input to layout panels
     * @param desiredSize
     */
    public void setDesiredSize(Size desiredSize) {
        this.desiredSize = desiredSize;
        this.setWidthConstraint(AxisConstraint.fixed(desiredSize.width));
        this.setHeightConstraint(AxisConstraint.fixed(desiredSize.height));
        desiredSizeDidChange();
    }

    public int getAxis(Axis axis) {
        if (axis == Axis.X)
            return getWidth();
        else
            return getHeight();
    }

    public void setAxis(Axis axis, int size) {
        if (axis == Axis.X)
            setWidth(size);
        else if (axis == Axis.Y)
            setHeight(size);
    }

    public void setWidth(int width) {
        resize(width, getConstrainedHeight(width));
    }

    public void setHeight(int height) {
        resize(getConstrainedWidth(height), height);
    }

    public int getSize(Axis axis) {
        if (axis == Axis.X)
            return width;
        else if (axis == Axis.Y)
            return height;

        return 0;
    }

    public AxisConstraint getDesiredSize(Axis axis, int assumedSize) {
        if (axis == Axis.X)
            return getDesiredWidth(assumedSize);
        else if (axis == Axis.Y)
            return getDesiredHeight(assumedSize);
        else
            return AxisConstraint.FREE;
    }

    private AxisConstraint widthConstraint = AxisConstraint.FREE;
    private AxisConstraint heightConstraint = AxisConstraint.FREE;

    public void setWidthConstraint(AxisConstraint widthConstraint) {
        this.widthConstraint = widthConstraint;
    }

    public void setHeightConstraint(AxisConstraint heightConstraint) {
        this.heightConstraint = heightConstraint;
    }

    public AxisConstraint getDesiredWidth(int assumedHeight) {
        return widthConstraint;
    }

    public AxisConstraint getDesiredHeight(int assumedWidth) {
        return heightConstraint;
    }

    public int getConstrainedAxis(Axis axis, int crossSize) {
        if (axis == Axis.X)
            return getConstrainedWidth(crossSize);
        else if (axis == Axis.Y)
            return getConstrainedHeight(crossSize);

        return 0;
    }

    public int getConstrainedWidth(int height) {
        return width;
    }

    public int getConstrainedHeight(int width) {
        return height;
    }

    /**
     * Determines what size this widget would *like* to have. This is used
     * as an input for panels which can use it, such as the Vertical/HorizontalLayoutPanel
     *
     * @return
     */
    @Deprecated
    public Size getDesiredSize() {
        return this.desiredSize;
    }

    private Size growScale;

    public void setGrowScale(int growScale) {
        this.growScale = new Size(growScale, growScale);
    }

    public void setGrowScale(Size growScale) {
        this.growScale = growScale;
    }

    public int getGrowScale(Axis axis) {
        var scale = getGrowScale();
        if (scale == null)
            return 0;

        if (axis == Axis.X)
            return scale.width;
        else if (axis == Axis.Y)
            return scale.height;

        return 0;
    }

    public Size getGrowScale() {
        if (growScale == null)
            return null;

        return growScale.copy();
    }
}
