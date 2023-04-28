package org.torchmc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.torchmc.layout.Panel;
import org.torchmc.util.Color;
import org.torchmc.util.ResizeMode;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class TorchScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public TorchScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, int width, int height) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = width;
        this.imageHeight = height;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    private double moveStartLeftPos = 0;
    private double moveStartTopPos = 0;
    private int resizeStartWidth = 0;
    private int resizeStartHeight = 0;
    private boolean resizing = false;
    private boolean moving = false;
    private ResizeMode resizeMode;
    private boolean wasMoved = false;
    private boolean initialized = false;
    private Panel panel;
    private boolean resizable = true;
    private boolean movable = true;
    private Size minSize = new Size(50, 50);
    private List<Window> windows = new ArrayList<>();
    private Window mainWindow;

    public Window getMainWindow() {
        return mainWindow;
    }

    protected void setMinSize(Size size) {
        minSize = size;
    }

    /**
     * True if the mouse is currently down on this widget.
     */
    protected boolean mouseDown = false;

    /**
     * X coordinate of the mouse click that is currently in progress (or the last mouse click if no click is currently
     * in progress. Useful to obtain the start position for drag operations.
     */
    protected double clickX = 0;

    /**
     * Y coordinate of the mouse click that is currently in progress (or the last mouse click if no click is currently
     * in progress. Useful to obtain the start position for drag operations.
     */
    protected double clickY = 0;

    /**
     * How tall the title bar is
     */
    protected int titlebarHeight = 18;

    /**
     * How wide the drag area is on the window
     */
    protected int dragHandleSize = 4;

    /**
     * Whether to render the Inventory label provided by Vanilla Minecraft.
     * Use inventoryLabelX/Y to position it.
     */
    protected boolean enableInventoryLabel = false;

    /**
     * What amount of the top portion of the window should receive a darker background color than the rest.
     * This is typically used to create a visual separation between the machine's UI and the user's inventory.
     */
    protected int twoToneHeight = 0;

    @Override
    protected final void init() {
        if (!initialized || !wasMoved) {
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.topPos = (this.height - this.imageHeight) / 2;
            initialized = true;
        }

        mainWindow = addChild(new ScreenWindow());

        setup();

        if (mainWindow != null) {
            mainWindow.move(leftPos, topPos, imageWidth, imageHeight);
        }
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        super.resize(pMinecraft, pWidth, pHeight);
    }

    protected void setup() {

    }

    public int getMaxZ() {
        int highZ = 0;
        for (var window : windows) {
            highZ = Math.max(highZ, window.z);
        }

        highZ = Math.max(highZ, z);
        return highZ;
    }

    protected <T extends Panel> T setPanel(T panel) {
        return setPanel(panel, p -> {});
    }

    protected <T extends WidgetBase> T addChild(T widget) {
        return addChild(widget, w -> {});
    }

    protected <T extends WidgetBase> T addChild(T widget, Consumer<T> initializer) {
        if (widget instanceof Window window) {
            window.z = getMaxZ() + 1;
            windows.add(window);
        } else {
            addRenderableWidget(widget);
        }

        widget.runInitializer(() -> initializer.accept(widget));
        //applyDimensions();
        return widget;
    }

    /**
     * Set the primary panel for this Screen. If you use a primary panel, moving/resizing of the screen will be handled
     * by adjusting the position of this panel, as opposed to calling rebuildWidgets(). This leads to smoother moving/resizing,
     * but means that you cannot use any standard Minecraft widgets as they do not support being moved in a uniform way.
     *
     * @param panel
     * @return
     * @param <T>
     */
    protected <T extends Panel> T setPanel(T panel, Consumer<T> initializer) {
        return mainWindow.setPanel(panel, initializer);
//
//        if (this.panel != null) {
//            removeWidget(this.panel);
//        }
//
//        this.panel = panel;
//        addRenderableWidget(panel);
//
//        initializer.accept(panel);
//
//        applyDimensions();
//
//        return panel;
    }

    protected int panelMargin = 8;

    protected void applyDimensions() {
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        if (enableInventoryLabel)
            this.font.draw(pPoseStack, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);
    }

    private boolean hoveringMenuBar(double pMouseX, double pMouseY) {
        return leftPos < pMouseX && pMouseX < leftPos + imageWidth
                && topPos < pMouseY && pMouseY < topPos + titlebarHeight;
    }

    private boolean hoveringBottomEdge(double pMouseX, double pMouseY) {
        return getBottomEdgeStart() < pMouseY && pMouseY < getBottomEdgeStart() + dragHandleSize;
    }

    private int getRightEdgeStart() {
        return leftPos + imageWidth - dragHandleSize / 2;
    }

    private boolean hoveringRightEdge(double mouseX, double mouseY) {
        return getRightEdgeStart() < mouseX && mouseX < getRightEdgeStart() + dragHandleSize;
    }

    private int getBottomEdgeStart() {
        return topPos + imageHeight - dragHandleSize / 2;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        mouseDown = true;
        clickX = pMouseX;
        clickY = pMouseY;

//        if (movable) {
//            if (hoveringMenuBar(pMouseX, pMouseY)) {
//                moving = true;
//                moveStartLeftPos = leftPos;
//                moveStartTopPos = topPos;
//                return true;
//            }
//        }
//
//        if (resizable) {
//            if (hoveringBottomEdge(pMouseX, pMouseY) && hoveringRightEdge(pMouseX, pMouseY)) {
//                resizing = true;
//                resizeMode = ResizeMode.BOTTOM_RIGHT;
//                resizeStartWidth = imageWidth;
//                resizeStartHeight = imageHeight;
//                return true;
//            } else if (hoveringBottomEdge(pMouseX, pMouseY)) {
//                resizing = true;
//                resizeMode = ResizeMode.BOTTOM;
//                resizeStartWidth = imageWidth;
//                resizeStartHeight = imageHeight;
//                return true;
//            } else if (hoveringRightEdge(pMouseX, pMouseY)) {
//                resizing = true;
//                resizeMode = ResizeMode.RIGHT;
//                resizeStartWidth = imageWidth;
//                resizeStartHeight = imageHeight;
//                return true;
//            }
//        }



        boolean handled = super.mouseClicked(pMouseX, pMouseY, pButton);

        // Wouldn't this make sense? But no, Mojank strikes again.
        // if (handled)
        //    return true;

        for (int i = windows.size() - 1; i >= 0; --i) {
            var window = windows.get(i);

            if (window.isMouseOver(pMouseX, pMouseY)) {
                if (window.mouseClicked(pMouseX, pMouseY, pButton)) {
                    this.setFocused(window);
                    if (pButton == 0) {
                        this.setDragging(true);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double pMouseX, double pMouseY) {
        for (int i = windows.size() - 1; i >= 0; --i) {
            var window = windows.get(i);

            if (window.isHovered()) {
                return Optional.of(window);
            }
        }

        return super.getChildAt(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

//        if (resizing) {
//            switch (resizeMode) {
//                case RIGHT -> {
//                    imageWidth = (int)(resizeStartWidth + (pMouseX - clickX));
//                }
//                case BOTTOM -> {
//                    imageHeight = (int)(resizeStartHeight + (pMouseY - clickY));
//                }
//                case BOTTOM_RIGHT -> {
//                    imageWidth = (int)(resizeStartWidth + (pMouseX - clickX));
//                    imageHeight = (int)(resizeStartHeight + (pMouseY - clickY));
//                }
//            }
//
//            imageWidth = Math.max(minSize.width, imageWidth);
//            imageHeight = Math.max(minSize.height, imageHeight);
//
//            if (!wasMoved) {
//                leftPos = (width - imageWidth) / 2;
//                topPos = (height - imageHeight) / 2;
//            }
//
//            if (panel != null) {
//                applyDimensions();
//            } else {
//                rebuildWidgets();
//            }
//
//            return true;
//        } else if (moving) {
//            leftPos = (int)(moveStartLeftPos + (pMouseX - clickX));
//            topPos = (int)(moveStartTopPos + (pMouseY - clickY));
//            wasMoved = true;
//
//            if (panel != null) {
//                applyDimensions();
//            } else {
//                rebuildWidgets();
//            }
//
//            return true;
//        }

        // First, handle the ContainerScreen mouse drag stuff. This *always* returns true and does not call super(),
        // because Mojang does not know how to make a user interface framework.

        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        // Now, do the important part that enables widgets to actually use this effing event

        return this.getFocused() != null && this.isDragging() && pButton == 0
                ? this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)
                : false
                ;
    }

    public boolean hoveringOverMainWindow(double pMouseX, double pMouseY) {
        return leftPos < pMouseX && pMouseX < leftPos + imageWidth && topPos < pMouseY && pMouseY < topPos + imageHeight;
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
//        TorchUtil.insetBox(
//                pPoseStack,
//                 TorchUI.builtInTex("gui/widgets/screen_background.png"),
//                leftPos, topPos, imageWidth, imageHeight
//        );
//
//        if (twoToneHeight > 0) {
//            TorchUtil.insetBox(
//                    pPoseStack,
//                    TorchUI.builtInTex("gui/widgets/twotone_background.png"),
//                    leftPos, topPos, imageWidth, twoToneHeight
//            );
//        }
//
//        renderTitleBar(pPoseStack, pPartialTick, pMouseX, pMouseY);
    }

    protected void renderTitleBar(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var border = 2;

        if (movable) {
            colorQuad(
                    poseStack, 0xFF999999,
                    leftPos + border, topPos + border,
                    imageWidth - border * 2, titlebarHeight - border * 2
            );
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        mouseDown = false;
        resizing = false;
        moving = false;

        boolean wasDragging = isDragging();
        var focusedListener = getFocused();
        boolean result = super.mouseReleased(pMouseX, pMouseY, pButton);

        // Similar (but opposite) to mouseDragged(), we actually *do* need to deliver mouse release events to whatever the
        // dragged widget is, so that they can release their hover state. Seriously, this is basic UI framework stuff, but
        // here we are. We could work around it like we do in mouseDragged(), but Forge, ever helpful, makes AbstractContainerMenu
        // call super. :-\

        if (wasDragging) {
            var hovered = this.getChildAt(pMouseX, pMouseY).orElse(null);
            if (hovered != focusedListener) {
                // There we go. Phew. Threading a needle.
                focusedListener.mouseReleased(pMouseX, pMouseY, pButton);
                return true;
            }
        }

        return result;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (getFocused() instanceof EditBox && ((EditBox) getFocused()).isFocused() && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            return false;
        }

        if (pKeyCode == GLFW.GLFW_KEY_TAB) {
            boolean direction = !hasShiftDown();

            // Try to move focus to the next/previous focusable widget.

            if (!this.changeFocus(direction)) {
                // This means there are no more widgets to focus, and the currently focused widget has now become null.
                // Wrap around to the beginning of the list, because changeFocus is supposed to focus on the first focusable
                // widget when there is no existing widget focused.

                this.changeFocus(direction);
            }

            return false;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

//    @Override
//    public boolean charTyped(char pCodePoint, int pModifiers) {
//        boolean textFocused = false;
//
//        if (!textFocused || pCodePoint != 'e')
//            return super.charTyped(pCodePoint, pModifiers);
//
//        return false;
//    }

    /**
     * Responsible for updating UI widgets state based on state changes that happen in the Menu.
     */
    public void updateStateFromMenu() {

    }

    private int z = 0;

    @Override
    public final void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableDepthTest();
        updateStateFromMenu();

        renderBackground(pPoseStack);

        for (var window : windows) {
            window.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        pPoseStack.pushPose();
        pPoseStack.translate(leftPos, topPos, 0);
        RenderSystem.applyModelViewMatrix();
        renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
        pPoseStack.popPose();
        RenderSystem.applyModelViewMatrix();

//        if (!resizing && resizable) {
//            if (hoveringRightEdge(pMouseX, pMouseY) && hoveringBottomEdge(pMouseX, pMouseY)) {
//                TorchUtil.colorQuad(
//                        pPoseStack, Color.WHITE.withAlpha(0.5f),
//                        getRightEdgeStart(), getBottomEdgeStart(),
//                        dragHandleSize, dragHandleSize
//                );
//            } else if (hoveringRightEdge(pMouseX, pMouseY)) {
//                TorchUtil.colorQuad(
//                        pPoseStack, Color.WHITE.withAlpha(0.5f),
//                        getRightEdgeStart(), topPos,
//                        dragHandleSize, imageHeight
//                );
//            } else if (hoveringBottomEdge(pMouseX, pMouseY)) {
//                TorchUtil.colorQuad(
//                        pPoseStack, Color.WHITE.withAlpha(0.5f),
//                        leftPos, getBottomEdgeStart(),
//                        imageWidth, dragHandleSize
//                );
//            }
//        }

        renderOver(pPoseStack, pMouseX, pMouseY);
    }

    public void sendWindowToTop(Window window) {
        windows.remove(window);
        windows.add(window);
    }

    public void sendWindowToBottom(Window window) {
        windows.remove(window);
        windows.add(0, window);
    }

    /**
     * Render on top of all other drawing for this widget
     * @param poseStack
     * @param mouseX
     * @param mouseY
     */
    protected void renderOver(PoseStack poseStack, int mouseX, int mouseY) {

    }

    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
    }

    protected void drawItem(PoseStack poseStack, ItemStack stack, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.translate(0,0, 32);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        var tr = TorchUtil.getTranslation(poseStack.last().pose());
        this.setBlitOffset(200);
        this.itemRenderer.renderAndDecorateItem(stack, (int)tr.x() + x, (int)tr.y() + y);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;

        RenderSystem.disableDepthTest();
    }

    protected void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height) {
        TorchUtil.textureQuad(stack, location, x, y, width, height);
    }

    protected void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height, float minU, float minV, float maxU, float maxV) {
        TorchUtil.textureQuad(stack, location, x, y, width, height, minU, minV, maxU, maxV);
    }

    protected void colorQuad(PoseStack stack, int color, double x, double y, double width, double height) {
        TorchUtil.colorQuad(stack, color, x, y, width, height);
    }

    protected void colorQuad(PoseStack stack, float r, float g, float b, float a, double x, double y, double width, double height) {
        TorchUtil.colorQuad(stack, r, g, b, a, x, y, width, height);
    }

    /**
     * Override to tell JEI about parts of your screen that are not contained within the screen's primary rectangle
     * (as defined by leftPos/topPos/imageWidth/imageHeight)
     * @return
     */
    protected List<Rect2i> getJeiAreas() {
        var list = new ArrayList<Rect2i>();

        for (var window : windows) {
            list.add(window.getScreenRect());
        }

        return list;
    }

    public static class JeiHandler implements IGuiContainerHandler<TorchScreen<?>> {
        @Override
        public List<Rect2i> getGuiExtraAreas(TorchScreen<?> containerScreen) {
            return containerScreen.getJeiAreas();
        }
    }

    public class ScreenWindow extends Window {
        public ScreenWindow() {
            super(title);
        }

        @Override
        protected void wasMovedByUser(int x, int y) {
            wasMoved = true;
        }

        @Override
        protected void didMove() {
            leftPos = x;
            topPos = y;

            TorchScreen.this.applyDimensions();
            super.didMove();
        }

        @Override
        protected void didResize() {
            imageWidth = width;
            imageHeight = height;

            if (!wasMoved) {
                mainWindow.move((TorchScreen.this.width - imageWidth) / 2, (TorchScreen.this.height - imageHeight) / 2);
            }

            TorchScreen.this.applyDimensions();
            super.didResize();
        }
    }
}
