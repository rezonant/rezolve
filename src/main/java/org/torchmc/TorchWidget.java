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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class of all Torch widgets.
 */
public abstract class TorchWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public TorchWidget(Component narrationTitle) {
        this.narrationTitle = narrationTitle;

        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> acScreen)
            this.screen = acScreen;

        minecraft = Minecraft.getInstance();
        font = minecraft.font;
    }

    private boolean visible = true;
    private TorchWidget parent;
    protected AbstractContainerScreen screen;
    private boolean isDecoration = false;
    private boolean hovered = false;
    private boolean focused = false;
    private boolean active = true;
    private List<Component> tooltip = null;
    private Runnable onTick = null;

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Minecraft minecraft;
    protected Font font;

    private int leftPadding = 0;
    private int rightPadding = 0;
    private int topPadding = 0;
    private int bottomPadding = 0;

    public int getPadding(Axis axis) {
        if (axis == Axis.X)
            return leftPadding + rightPadding;
        else if (axis == Axis.Y)
            return topPadding + bottomPadding;

        return 0;
    }

    public int getLeftPadding() {
        return leftPadding;
    }

    public int getTopPadding() {
        return topPadding;
    }

    public int getRightPadding() {
        return rightPadding;
    }

    public int getBottomPadding() {
        return bottomPadding;
    }

    /**
     * Change the amount of padding pixels requested to be added to the left of the widget. These pixels will not be
     * included in the observable width of the widget. This only takes effect when this widget is placed into an appropriate
     * container (for instance AxisLayoutContainer and its derivatives).
     * @param leftPadding
     */
    public void setLeftPadding(int leftPadding) {
        this.leftPadding = leftPadding;
    }

    /**
     * Change the amount of padding pixels requested to be added to the right of the widget. These pixels will not be
     * included in the observable width of the widget. This only takes effect when this widget is placed into an appropriate
     * container (for instance AxisLayoutContainer and its derivatives).
     * @param rightPadding
     */
    public void setRightPadding(int rightPadding) {
        this.rightPadding = rightPadding;
    }

    /**
     * Change the amount of padding pixels requested to be added to the top of the widget. These pixels will not be
     * included in the observable height of the widget. This only takes effect when this widget is placed into an appropriate
     * container (for instance AxisLayoutContainer and its derivatives).
     * @param topPadding
     */
    public void setTopPadding(int topPadding) {
        this.topPadding = topPadding;
    }

    /**
     * Change the amount of padding pixels requested to be added to the bottom of the widget. These pixels will not be
     * included in the observed height of the widget. This only takes effect when this widget is placed into an appropriate
     * container (for instance AxisLayoutContainer and its derivatives).
     * @param bottomPadding
     */
    public void setBottomPadding(int bottomPadding) {
        this.bottomPadding = bottomPadding;
    }

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

    /**
     * Set the tooltip to be shown when the user hovers over this widget.
     * @param tooltip
     */
    public void setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Set the tooltip to be shown when the user hovers over this widget.
     * @param tooltip
     */
    public void setTooltip(Component tooltip) {
        this.tooltip = List.of(tooltip);
    }

    /**
     * Set the tooltip to be shown when the user hovers over this widget.
     * @param text
     */
    public void setTooltip(String text) {
        this.tooltip = List.of(Component.literal(text));
    }

    /**
     * Supply a function which will be run before the widget is rendered. This can be used to add simple logic to
     * continuously update the state of the widget. Since it runs per frame, you should not perform expensive operations
     * in this function.
     * @param onTick
     */
    public void setOnTick(Runnable onTick) {
        this.onTick = onTick;
    }

    public Runnable getOnTick() {
        return onTick;
    }

    /**
     * Find the screen rect for the widget's current location and size.
     * @return
     */
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

    /**
     * Find the screen rect representing the current location and desired size of this widget.
     * @return
     */
    public Rect2i getDesiredScreenRect() {
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

    /**
     * When set to true, clipping to the screen and parent widget dimensions will not occur, as it does by default.
     * This should be used if the widget needs to be rendered outside the frame of its parent. For example, if you
     * wish to add a TorchWidget as a child of a TorchScreen's main window but position it outside the bounds of that
     * window, you will need to set this in order for the widget to be rendered.
     * @param decoration
     */
    public void setIsDecoration(boolean decoration) {
        isDecoration = decoration;
    }

    /**
     * Move this widget to a specific location with respect to its parent's frame. If the supplied parameters cause
     * an actual change to the position of this widget, the didMove() lifecycle event is fired.
     *
     * @param x The X position relative to the parent widget's position
     * @param y The Y position relative to the parent widget's position.
     */
    public final void move(int x, int y) {
        if (this.x == x && this.y == y)
            return;

        this.x = x;
        this.y = y;
        didMove();
    }

    /**
     * Called whenever this widget's position changes.
     */
    protected void didMove() {

    }

    /**
     * Called whenever this widget's size changes. Override this to add special behavior. This is overridden in
     * Torch's LayoutPanel widgets to handle reflowing children to conform to the new size.
     */
    protected void didResize() {

    }

    /**
     * List of child widgets contained within this widget.
     */
    protected List<TorchWidget> children = new ArrayList<>();

    /**
     * Called by Torch internally to set the parent widget for this widget.
     * @param parent
     */
    void adoptParent(TorchWidget parent) {
        this.parent = parent;
    }

    /**
     * Add a child widget to this widget.
     *
     * @param widget The widget to add
     * @return A reference to the passed widget, for chaining purposes or obtaining the created reference
     * @param <T> The type of widget
     */
    public <T extends TorchWidget> T addChild(T widget) {
        return addChild(widget, w -> {});
    }

    /**
     * Add a childs widget to this widget, and run the given initializer on it. The initializer runs before the
     * hierarchyDidChange() lifecycle hook is fired, so it is the ideal way to initialize a child widget without
     * performing unnecessary layout reflows when using LayoutPanel and its derivatives.
     *
     * Example: addChild(new HorizontalPanel(), panel -> panel.setGrowSize(1))
     *
     * @param widget The widget to add
     * @param initializer The initializer to run. Receives one argument: the widget passed to addChild()
     * @return A reference to the passed widget, for chaining purposes or obtaining the created reference
     * @param <T> The type of widget
     */
    public <T extends TorchWidget> T addChild(T widget, Consumer<T> initializer) {
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
     * Called when the setWidthConstraint() or setHeightConstraint() methods are called to allow the widget to react.
     * The default implementation notifies parents that the getDesiredSize() has changed in case they are interested.
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

    /**
     * Obtain the children of this widget from an external context.
     * @return An array of the current children of this widget.
     */
    public TorchWidget[] getChildren() {
        return children.toArray(new TorchWidget[children.size()]);
    }

    /**
     * Check if the given point (in units relative to the parent widget) falls within the bounds of this widget.
     * This is a vanilla Minecraft API which you do not generally need to reimplement, Torch's implementation should
     * be sufficient.
     *
     * @param pMouseX The X coordinate within the rectangle of this widget's parent widget
     * @param pMouseY The Y coordinate within the rectangle of this widget's parent widget
     *
     * @return True if the given position falls within the bounds of this widget
     */
    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return visible && x < pMouseX && pMouseX < x + width && y < pMouseY && pMouseY < y + height;
    }

    /**
     * True if this widget is currently hovered or focused, based on the mouse position during the last rendered frame.
     * @return
     */
    public boolean isHoveredOrFocused() {
        return hovered || focused;
    }

    /**
     * True if this widget is hovered, based on the mouse position during the last rendered frame.
     * @return
     */
    public boolean isHovered() {
        return hovered;
    }

    /**
     * True if this widget currently has keyboard focus (tab focus).
     * @return
     */
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

    /**
     * Change whether this widget can be interacted with by the user. This includes mouse and keyboard interaction.
     * @param active
     */
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

    private TorchWidget focusedChild;
    private boolean focusable = false;

    /**
     * True if this widget is itself focusable. Note that this is distinct from the widget being able to *handle* focus
     * amongst its children, all widgets can do that.
     * @return
     */
    public boolean isFocusable() {
        return focusable;
    }

    /**
     * Change whether this widget is capable of receiving the current keyboard focus. Defaults to false.
     * @param focusable
     */
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
     * Make this widget take the current focus. This will both set the focus state of this widget and make the
     * top level Screen reflect the same.
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

            if (renderable instanceof TorchWidget widget) {
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

    /**
     * Called when the user types a character via the keyboard while this widget is focused.
     * @param pCodePoint The character typed
     * @param pModifiers Modifier keys that are pressed. See GLFW class for keyboard constants.
     * @return
     */
    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (focusedChild != null)
            return focusedChild.charTyped(pCodePoint, pModifiers);
        return false;
    }

    /**
     * Called when the user presses a keyboard key down and this widget has focus.
     * @param pKeyCode The keyboard key pressed. See GLFW class for keyboard constants
     * @param pScanCode The scan code of the key
     * @param pModifiers The modifiers being pressed.
     * @return
     */
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (focusedChild != null)
            return focusedChild.keyPressed(pKeyCode, pScanCode, pModifiers);
        return false;
    }

    /**
     * Called when the user releases a keyboard key and this widget has focus.
     *
     * @param pKeyCode
     * @param pScanCode
     * @param pModifiers
     * @return
     */
    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (focusedChild != null)
            return focusedChild.keyReleased(pKeyCode, pScanCode, pModifiers);
        return false;
    }

    /**
     * Called when this widget becomes focused/unfocused.
     * @param focused
     */
    public void focusStateChanged(boolean focused) {

    }

    /**
     * Called when this widget gains the current focus.
     */
    public void becameFocused() {

    }

    /**
     * Called when this widget loses the current focus.
     */
    public void becameUnfocused() {

    }

    /**
     * Change whether this widget is considered visible or not. Defaults to true. Widgets which are invisible:
     * - Are not rendered (by default)
     * - Are not allocated space in layout routines
     * - Cannot receive focus
     * @param visible
     */
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

    /**
     * True if this widget is currently configured to be visible.
     * @return
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Called when this widget becomes visible (see setVisible)
     */
    public void didBecomeVisible() {

    }

    /**
     * Called when this widget becomes invisible (see setVisible)
     */
    public void didBecomeInvisible() {

    }

    /**
     * Set the position and size of this widget. Equivalent to running move() followed by resize().
     * @param x The X coordinate relative to the parent widget's rectangle
     * @param y The Y coordinate relative to the parent widget's rectangle
     * @param width The new width of the widget
     * @param height The new height of the widget
     */
    public final void move(int x, int y, int width, int height) {
        move(x, y);
        resize(width, height);
    }

    /**
     * Set the size of the widget. If the passed parameters result in a change to the size, the didResize() lifecycle
     * hook is called.
     * @param width The new width of the widget in virtual pixels (including current GUI scale settings)
     * @param height The new height of the widget in virtual pixels (including current GUI scale settings)
     */
    public final void resize(int width, int height) {
        if (this.width == width && this.height == height)
            return;

        this.width = width;
        this.height = height;
        didResize();
    }

    /**
     * The title read to the user when this widget is observed during Narrator mode.
     */
    protected Component narrationTitle;

    /**
     * Render this widget within the current context.
     *
     * TorchWidgets cannot override this method. Instead, override:
     * - renderContents() to perform general custom rendering
     * - renderBackground() to modify early rendering of the widget (before children are rendered)
     * - renderChildren() to modify how the children of this widget are rendered
     *
     * @param pPoseStack
     * @param pMouseX Current mouse X position relative to the parent's bounding box (not screen coordinates)
     * @param pMouseY Current mouse Y position relative to the parent's bounding box (not screen coordinates)
     * @param pPartialTick
     */
    @Override
    public final void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.onTick != null)
            this.onTick.run();

        if (!visible)
            return;

        hovered = isMouseOver(pMouseX, pMouseY);

        pushPose(pPoseStack, () -> {
            repose(() -> pPoseStack.translate(0, 0, 0));

            screenScissor(() -> {
                renderBackground(pPoseStack, pMouseX, pMouseY, pPartialTick);
                pushPose(pPoseStack, () -> {
                    repose(() -> pPoseStack.translate(x, y, 0));
                    renderChildren(pPoseStack, pMouseX - x, pMouseY - y, pPartialTick);
                });
                renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
            });
        });

        if (isHovered())
            renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    /**
     * Render parts of this widget which should be rendered below child widgets.
     *
     * @param pPoseStack
     * @param pMouseX Current mouse position relative to the parent's bounding box (not the screen)
     * @param pMouseY Current mouse position relative to the parent's bounding box (not the screen)
     * @param pPartialTick
     */
    protected void renderBackground(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
    }

    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        var tooltip = getTooltip(); // Important to allow customization of tooltip behavior in widget classes
        if (tooltip != null && tooltip.size() > 0 && screen != null)
            screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
    }

    /**
     * Render the children attached to this widget. Can be overridden to customize, but the default implementation
     * should be correct in nearly all cases. Runs after renderBackground() but before renderContents().
     * @param pPoseStack
     * @param pMouseX The current mouse position relative to the parent's bounding box (not the screen)
     * @param pMouseY The current mouse position relative to the parent's bounding box (not the screen)
     * @param pPartialTick
     */
    protected void renderChildren(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        for (var child : children) {
            child.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    /**
     * Perform custom rendering for this widget. Can be overriden to customize if custom rendering is required.
     * This is performed after renderBackground() and renderChildren(), so it is effectively the "top" layer of
     * the rendering process.
     *
     * @param pPoseStack
     * @param pMouseX
     * @param pMouseY
     * @param pPartialTick
     */
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

    }

    /**
     * Obtain the narration priority for this widget. Defaults to HOVERED
     * @return
     */
    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    /**
     * Called by Narrator mode to update the narrator's view of this widget. Implementations should attach narration
     * elements to the passed NarrationElementOutput instance. The default implementation adds `narrationTitle` as
     * a TITLE element, which is sufficient for many use cases.
     *
     * @param pNarrationElementOutput
     */
    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, narrationTitle);
    }

    public TorchWidget getRootParent() {
        TorchWidget widget = this;
        while (widget.parent != null)
            widget = widget.parent;

        return widget;
    }

    /**
     * Perform a scissor (clip) against the bounding box of the screen which contains this widget, runs the given
     * function in that context, and finally removes the scissor (clip) state.
     * @param runnable
     */
    protected void screenScissor(Runnable runnable) {
        var border = 2;
        var root = getRootParent();

        if (screen != null && !isDecoration()) {
            if (root instanceof Window window) {
                var rect = window.getScreenRect();
                displayScissor(
                        rect.getX() + border, rect.getY() + border,
                        rect.getWidth() - border * 2, rect.getHeight() - border * 2,
                        runnable);
                return;
            }

            displayScissor(
                    screen.getGuiLeft() + border, screen.getGuiTop() + border,
                    screen.getXSize() - border * 2, screen.getYSize() - border * 2,
                    runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Control whether scissor operations are used globally. Useful for debugging issues with coordinates, where
     * something appears not to render, but is actually rendered outside scissor bounds due to coordinate issues.
     */
    public static boolean enableScissoring = true;

    /**
     * Perform a scissor (clip) operation with the given bounding box specified with coordinates relative to the passed
     * PoseStack. Note that calling this from one of TorchWidget's render methods will require coordinates to be relative
     * to the parent widget's bounding box. Once the clip operation is set, the given function is called, and finally
     * the scissor (clip) is cleared before returning.
     *
     * @param stack The current PoseStack. Will be used to adjust the passed coordinates to match its current state
     * @param x X coordinate of the box
     * @param y Y coordinate of the box
     * @param width Width of the box (not the coordinate of the right-side point like vanilla's enableScissor)
     * @param height Height of the box (not the coordinate of the bottom-side point like vanilla's enableScissor)
     * @param runnable
     */
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

    /**
     * Perform a scissor (clipping) operation with the given bounding box specified in terms of display coordinates
     * (that is, without respect to the current widget's coordinate space). Note that width/height are lengths, not
     * coordinates as they are in RenderSystem.enableScissor().
     *
     * After the clipping operation is set up, the given function is called, and finally the clipping operation is
     * removed before returning.
     *
     * @param x The X coordinate of the bounding box in global screen space
     * @param y The Y coordinate of the bounding box in global screen space
     * @param width The width of the bounding box (not the coordinate of the right side)
     * @param height The height of the bounding box (not the coordinate of the bottom side)_
     * @param runnable
     */
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

    /**
     * Push a pose onto the given PoseStack, call the given runnable, and finally pop the pose off the given PoseStack
     * before returning. Use this with repose()
     * @param stack
     * @param runnable
     */
    protected void pushPose(PoseStack stack, Runnable runnable) {
        stack.pushPose();
        try {
            runnable.run();
        } finally {
            stack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /**
     * Run the given runnable, then apply the current pose stack to the model view matrix before returning. Useful
     * for grouping operations that modify the current pose while still avoiding applyModelViewMatrix() boilerplate.
     *
     * @param runnable
     */
    protected void repose(Runnable runnable) {
        runnable.run();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * Called when the user scrolls up/down using the mouse scroll wheel.
     * @param pMouseX The current mouse coordinates, relative to the parent bounding box (not the screen)
     * @param pMouseY The current mouse coordinates, relative to the parent bounding box (not the screen)
     * @param pDelta How far the scroll wheel has moved since the last event
     * @return
     */
    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        pMouseX -= x;
        pMouseY -= y;

        for (var child : children) {
            if (child.isVisible() && child.isMouseOver(pMouseX, pMouseY)) {
                if (child.mouseScrolled(pMouseX, pMouseY, pDelta)) {
                    return true;
                }
            }
        }
        return GuiEventListener.super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    /**
     * Set to true by TorchWidget when a mouse button is currently pressed.
     */
    protected boolean mouseDown;

    /**
     * Set to the current click during TorchWidget's default mouseClicked() handler.
     * This value is not cleared, so if the user releases the mouse button, this will effectively be the
     * *last* mouse click.
     */
    protected double clickX;
    protected double clickY;

    /**
     * Called when the user presses a mouse button down within the bounds of this widget (as determined by isMouseOver()).
     *
     * @param pMouseX The mouse coordinates relative to the parent widget's bounding box (not the screen)
     * @param pMouseY The mouse coordinates relative to the parent widget's bounding box (not the screen)
     * @param pButton The button pressed down. 0 for left, 1 for middle, 2 for right.
     * @return
     */
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        mouseDown = true;

        // Pass to children, putting it into their parent coordinate space (ours)

        pMouseX -= x;
        pMouseY -= y;

        clickX = pMouseX;
        clickY = pMouseY;

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

    /**
     * Called when the user releases a mouse button which was previously observed via mouseClick(). The mouse does
     * not need to be within the bounds of this widget to receive this event as long as the mouseClick() happened within
     * the bounds of this widget.
     *
     * @param pMouseX Mouse position relative to the parent widget's bounding box (not the screen)
     * @param pMouseY Mouse position relative to the parent widget's bounding box (not the screen)
     * @param pButton The button that was released. 0 for left, 1 for middle, 2 for right.
     * @return
     */
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

    /**
     * Called when the user has a mouse button held down and the mouse is moved.
     *
     * @param pMouseX Mouse position relative to the parent widget's bounding box (not the screen)
     * @param pMouseY Mouse position relative to the parent widget's bounding box (not the screen)
     * @param pButton The button currently being pressed. 0 for left, 1 for middle, 2 for right.
     * @param pDragX How many pixels has the mouse moved since the last event
     * @param pDragY How many pixels has the mouse moved since the last event
     * @return True if the event was handled by the widget.
     */
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        pMouseX -= x;
        pMouseY -= y;

        if (draggedWidget != null && draggedWidget instanceof GuiEventListener listener) {
            listener.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return false;
    }

    /**
     * Called when the mouse has moved. When a mouse button is pressed, these events can be received when the mouse is
     * outside the widget bounds. If the mouse button is not pressed, this event is only received when isMouseOver()
     * returns true for the current mouse position.
     *
     * @param pMouseX Mouse position relative to parent bounding box (not screen)
     * @param pMouseY Mouse position relative to parent bounding box (not screen)
     */
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
     * Constrain this widget to the given fixed size.
     * @param desiredSize
     */
    public void setFixedSize(Size desiredSize) {
        this.setWidthConstraint(AxisConstraint.fixed(desiredSize.width));
        this.setHeightConstraint(AxisConstraint.fixed(desiredSize.height));
    }

    /**
     * Get the length of this widget's dimension across the given axis in virtual pixels.
     * @param axis
     * @return
     */
    public int getAxis(Axis axis) {
        if (axis == Axis.X)
            return getWidth();
        else
            return getHeight();
    }

    /**
     * Change the length of this widget's dimension across the given axis in virtual pixels.
     * @param axis
     * @param size
     */
    public void setAxis(Axis axis, int size) {
        if (axis == Axis.X)
            setWidth(size);
        else if (axis == Axis.Y)
            setHeight(size);
    }

    /**
     * Set the width of this widget. The height will be adjusted based on this width if needed (using getConstrainedHeight())
     * @param width
     */
    public void setWidth(int width) {
        resize(width, getConstrainedHeight(width));
    }

    /**
     * Set the height of this widget. The width will be adjusted based on this height if needed (using getConstrainedWidth())
     * @param height
     */
    public void setHeight(int height) {
        resize(getConstrainedWidth(height), height);
    }

    /**
     * Get the size constraint on the given axis, passing in the assumed size of the opposite axis for reference.
     * Reimplementations should appropriately consider the setConstraint() family of methods or restrict them.
     * Should only implement this method OR implement getWidthConstraint/getHeightConstraint, not both.
     *
     * @param axis The desired axis
     * @param assumedCrossSize Size of the opposite axis (or zero if unknown)
     * @return
     */
    public AxisConstraint getConstraint(Axis axis, int assumedCrossSize) {
        if (axis == Axis.X)
            return getWidthConstraint(assumedCrossSize);
        else if (axis == Axis.Y)
            return getHeightConstraint(assumedCrossSize);
        else
            return AxisConstraint.FREE;
    }

    private AxisConstraint widthConstraint = AxisConstraint.FREE;
    private AxisConstraint heightConstraint = AxisConstraint.FREE;

    /**
     * Set a constraint on the width of this widget
     * @param widthConstraint
     */
    public void setWidthConstraint(@Nonnull AxisConstraint widthConstraint) {
        if (widthConstraint == null)
            throw new IllegalArgumentException("widthConstraint cannot be null");

        this.widthConstraint = widthConstraint;
        desiredSizeDidChange();
    }

    /**
     * Set a constraint on the height of this widget
     * @param heightConstraint
     */
    public void setHeightConstraint(@Nonnull AxisConstraint heightConstraint) {
        if (heightConstraint == null)
            throw new IllegalArgumentException("heightConstraint cannot be null");
        this.heightConstraint = heightConstraint;
        desiredSizeDidChange();
    }

    /**
     * Get the width constraint, passing in the assumed size of the opposite axis for reference.
     * Reimplementations should appropriately consider the setConstraint() family of methods or restrict them.
     * Should only implement this method OR implement getConstraint, not both.
     *
     * @param assumedHeight
     * @return
     */
    public AxisConstraint getWidthConstraint(int assumedHeight) {
        return widthConstraint;
    }

    public AxisConstraint getHeightConstraint(int assumedWidth) {
        return heightConstraint;
    }

    /**
     * Get the "constrained" size of the given axis based on the size given for the opposite axis. This is used
     * when applying actual changes to the widget's dimensions to allow the widget to react to size changes.
     * Reimplementors should implement this OR getConstrainedWidth/getConstrainedHeight, not both.
     *
     * For instance a textbox widget might use this to ensure its height is changed to match the height of the text
     * flow based on changes to the width of the widget. Similarly, an aspect ratio locked image would use this to
     * constrain the width based on the height and the height based on the width.
     *
     * Note that this is orthogonal from the constraints applied to this widget.
     *
     * @param axis
     * @param crossSize
     * @return
     */
    public int getConstrainedAxis(Axis axis, int crossSize) {
        if (axis == Axis.X)
            return getConstrainedWidth(crossSize);
        else if (axis == Axis.Y)
            return getConstrainedHeight(crossSize);

        return 0;
    }

    /**
     * Get the "constrained" size of the given axis based on the size given for the opposite axis. This is used
     * when applying actual changes to the widget's dimensions to allow the widget to react to size changes.
     * Reimplementors should implement this OR getConstrainedWidth/getConstrainedHeight, not both.
     *
     * For instance a textbox widget might use this to ensure its height is changed to match the height of the text
     * flow based on changes to the width of the widget. Similarly, an aspect ratio locked image would use this to
     * constrain the width based on the height and the height based on the width.
     *
     * Note that this is orthogonal from the constraints applied to this widget.
     *
     * @param height
     * @return
     */
    public int getConstrainedWidth(int height) {
        var constraint = getWidthConstraint(height);

        if (constraint.fixed)
            return constraint.desired;

        return width;
    }

    /**
     * Get the "constrained" size of the given axis based on the size given for the opposite axis. This is used
     * when applying actual changes to the widget's dimensions to allow the widget to react to size changes.
     * Reimplementors should implement this OR getConstrainedWidth/getConstrainedHeight, not both.
     *
     * For instance a textbox widget might use this to ensure its height is changed to match the height of the text
     * flow based on changes to the width of the widget. Similarly, an aspect ratio locked image would use this to
     * constrain the width based on the height and the height based on the width.
     *
     * Note that this is orthogonal from the constraints applied to this widget.
     *
     * @param width
     * @return
     */
    public int getConstrainedHeight(int width) {
        var constraint = getHeightConstraint(width);

        if (constraint.fixed)
            return constraint.desired;

        return height;
    }

    /**
     * Determines what size this widget would *like* to have based on its constraints.
     * @return
     */
    public Size getDesiredSize() {
        var width = getWidthConstraint(0);
        var height = getHeightConstraint(0);

        return new Size(width.desired > 0 ? width.desired : width.min, height.desired > 0 ? height.desired : height.min);
    }

    private Size expansionFactor;

    /**
     * Set a uniform expansion factor for this widget, to be considered by LayoutPanels when allocating space in a layout.
     * Widgets which have equal expansion factors on an axis will split extra space fairly. Widgets which have a higher
     * expansion factors will receive more space than widgets with lower expansion factors.
     * @param growScale
     */
    public void setExpansionFactor(int growScale) {
        this.expansionFactor = new Size(growScale, growScale);
    }

    /**
     * Set expansion factor for this widget, to be considered by LayoutPanels when allocating space in a layout.
     * Widgets which have equal expansion factors on an axis will split extra space fairly. Widgets which have a higher
     * expansion factors will receive more space than widgets with lower expansion factors.
     * @param growScale
     */
    public void setExpansionFactor(Size growScale) {
        this.expansionFactor = growScale;
    }

    /**
     * Get the expansion factor defined via setExpansionFactor for the given axis
     * @param axis
     * @return
     */
    public int getExpansionFactor(Axis axis) {
        var scale = getExpansionFactor();
        if (scale == null)
            return 0;

        if (axis == Axis.X)
            return scale.width;
        else if (axis == Axis.Y)
            return scale.height;

        return 0;
    }

    /**
     * Get the X/Y expansion factor assigned to this widget.
     * @return
     */
    public Size getExpansionFactor() {
        if (expansionFactor == null)
            return null;

        return expansionFactor.copy();
    }
}
