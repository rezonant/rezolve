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
import org.torchmc.util.ResizeMode;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Superclass of screens which use the Torch UI and layout system. Such screens use Torch's windowing system,
 * so contents of the screen are added to the screen's "main window". Though children are actually parented to the
 * screen's main window, the main window's dimensions and position are automatically synced to the screen's size.
 * @param <T>
 */
public abstract class TorchScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public TorchScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, int width, int height) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = width;
        this.imageHeight = height;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    private boolean wasMoved = false;
    private boolean initialized = false;
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
     * Whether to render the Inventory label provided by Vanilla Minecraft.
     * Use inventoryLabelX/Y to position it.
     */
    protected boolean enableInventoryLabel = false;

    @Override
    protected final void init() {
        if (!initialized || !wasMoved) {
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.topPos = (this.height - this.imageHeight) / 2;
            initialized = true;
        }

        windows.clear();
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

    protected <T extends Panel> T setPanel(T panel) {
        return setPanel(panel, p -> {});
    }

    protected <T extends TorchWidget> T addChild(T widget) {
        return addChild(widget, w -> {});
    }

    protected <T extends TorchWidget> T addChild(T widget, Consumer<T> initializer) {
        if (widget instanceof Window window) {
            windows.add(window);
        } else {
            addRenderableWidget(widget);
        }

        widget.runInitializer(() -> initializer.accept(widget));
        return widget;
    }

    public void addWindow(Window window) {
        addChild(window);
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
    }

    protected void applyDimensions() {
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        mouseDown = true;
        clickX = pMouseX;
        clickY = pMouseY;

        super.mouseClicked(pMouseX, pMouseY, pButton);

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

        // First, handle the ContainerScreen mouse drag stuff. This *always* returns true and does not call super(),
        // because Mojang does not know how to make a user interface framework.

        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        // Now, do the important part that enables widgets to actually use this effing event

        return this.getFocused() != null && this.isDragging() && pButton == 0
                ? this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)
                : false
                ;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        mouseDown = false;

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

    /**
     * Responsible for updating UI widgets state based on state changes that happen in the Menu.
     */
    public void updateStateFromMenu() {

    }

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
            if (window.isVisible())
                list.add(window.getScreenRect());
        }

        return list;
    }

    public static class JeiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
        @Override
        public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
            List<Rect2i> list = new ArrayList<>();
            for (var child : containerScreen.children()) {
                if (child instanceof Window window && window.isVisible()) {
                    list.add(((Window) child).getScreenRect());
                }
            }

            if (containerScreen instanceof TorchScreen<?> torchScreen)
                list.addAll(torchScreen.getJeiAreas());

            return list;
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
        public void onClose() {
            TorchScreen.this.onClose();
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
