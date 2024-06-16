package org.torchmc.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.torchmc.events.Event;
import org.torchmc.events.EventEmitter;
import org.torchmc.events.EventType;
import org.torchmc.events.Subscription;
import org.torchmc.ui.layout.Panel;
import org.torchmc.ui.util.Size;
import org.torchmc.ui.util.TorchUtil;
import org.torchmc.ui.widgets.SlotWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Superclass of screens which use the Torch UI and layout system. Such screens use Torch's windowing system,
 * so contents of the screen are added to the screen's "main window". Though children are actually parented to the
 * screen's main window, the main window's dimensions and position are automatically synced to the screen's size.
 * @param <T>
 */
public abstract class TorchScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements EventEmitter {
    public TorchScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, int width, int height) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = width;
        this.imageHeight = height;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    public static final EventType<Event> CLOSED = new EventType<Event>();

    private boolean wasMoved = false;
    private boolean initialized = false;
    private Size minSize = new Size(50, 50);
    private List<Window> windows = new ArrayList<>();
    private Window mainWindow;
    private EventEmitter.EventMap eventMap = new EventEmitter.EventMap();

    @Override
    public EventMap eventMap() {
        return eventMap;
    }

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
        setupMainWindow();
        setup();

    }

    private void setupMainWindow() {
        mainWindow = addChild(createMainWindow());
        mainWindow.move(leftPos, topPos, imageWidth, imageHeight);

        mainWindow.addEventListener(Window.MOVED_BY_USER, e -> wasMoved = true);
        mainWindow.addEventListener(Window.CLOSED, e -> TorchScreen.this.onClose());
        mainWindow.addEventListener(Window.MOVED, e -> {
            leftPos = e.x;
            topPos = e.y;
            TorchScreen.this.applyDimensions();
        });
        mainWindow.addEventListener(Window.RESIZED, e -> {
            imageWidth = e.width;
            imageHeight = e.height;

            if (!wasMoved) {
                mainWindow.move((TorchScreen.this.width - imageWidth) / 2, (TorchScreen.this.height - imageHeight) / 2);
            }

            TorchScreen.this.applyDimensions();
        });
    }

    @Override
    public final void onClose() {
        super.onClose();
        emitEvent(CLOSED);
        wasClosed();
    }

    /**
     * Called when this screen has been closed.
     */
    protected void wasClosed() {

    }

    /**
     * Unsubscribe the given subscription when this widget is destroyed.
     * @param subscription
     */
    public void removeWhenClosed(Subscription subscription) {
        addEventListener(CLOSED, e -> subscription.unsubscribe());
    }

    protected Window createMainWindow() {
        return new Window(title);
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

    public <T extends Window> T addWindow(T window) {
        return addWindow(window, w -> {});
    }

    public <T extends Window> T addWindow(T window, Consumer<T> initializer) {
        return addChild(window, initializer);
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
    protected void renderLabels(@NotNull GuiGraphics gfx, int pMouseX, int pMouseY) {
        // Intentionally do nothing. Do not remove.
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

                break;
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

    public Window getWindowAt(double mouseX, double mouseY) {
        for (int i = windows.size() - 1; i >= 0; --i) {
            var window = windows.get(i);
            if (window.isMouseOver(mouseX, mouseY))
                return window;
        }

        return null;
    }

    @Nullable
    @Override
    protected Slot findSlot(double pMouseX, double pMouseY) {
        var window = getWindowAt(pMouseX, pMouseY);

        if (window == null)
            return null;

        AtomicReference<Slot> slot = new AtomicReference<>();

        window.visit(w -> {
            if (w instanceof SlotWidget possibleSlot) {
                var mcSlot = possibleSlot.getSlot();
                var mcSlotRect = new Rect2i(leftPos + mcSlot.x, topPos + mcSlot.y, 16, 16);
                if (mcSlotRect.contains((int)pMouseX, (int)pMouseY)) {
                    slot.set(mcSlot);
                    return false;
                }
            }
            return true;
        });

        return slot.get();
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

        // TODO: hoping this won't be needed anymore
//        if (pKeyCode == GLFW.GLFW_KEY_TAB) {
//            boolean direction = !hasShiftDown();
//
//            // Try to move focus to the next/previous focusable widget.
//
//            if (!this.changeFocus(direction)) {
//                // This means there are no more widgets to focus, and the currently focused widget has now become null.
//                // Wrap around to the beginning of the list, because changeFocus is supposed to focus on the first focusable
//                // widget when there is no existing widget focused.
//
//                this.changeFocus(direction);
//            }
//
//            return false;
//        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    /**
     * Responsible for updating UI widgets state based on state changes that happen in the Menu.
     */
    public void updateStateFromMenu() {

    }

    @Override
    public final void render(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        //RenderSystem.enableDepthTest();
        updateStateFromMenu();

        renderBackground(gfx);

        gfx.pose().pushPose();
        var windowUnderCursor = getWindowAt(pMouseX, pMouseY);
        for (var window : windows) {
            boolean receiveCursor = windowUnderCursor == null || windowUnderCursor == window;
            window.render(gfx, receiveCursor ? pMouseX : -1, receiveCursor ? pMouseY : -1, pPartialTick);
            gfx.pose().translate(0, 0, 200.0f); // TODO: is this still an appropriate amount to push?
        }
        gfx.pose().popPose();

        // Background
        renderBg(gfx, pPartialTick, pMouseX, pMouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.Render.Background(
                this, gfx, pMouseX, pMouseY
        ));

        // Children
        RenderSystem.disableDepthTest();
        for(Renderable widget : this.renderables)
            widget.render(gfx, pMouseX, pMouseY, pPartialTick);

        // Foreground
        gfx.pose().pushPose();
        gfx.pose().translate(leftPos, topPos, 0);
        RenderSystem.applyModelViewMatrix();
        renderContents(gfx, pMouseX, pMouseY, pPartialTick);
        gfx.pose().popPose();
        RenderSystem.applyModelViewMatrix();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.Render.Foreground(
                this, gfx, pMouseX, pMouseY
        ));

        // Overlay
        renderOver(gfx, pMouseX, pMouseY);
        renderCarriedItem(gfx, pMouseX, pMouseY, pPartialTick);

        // Cleanup
        RenderSystem.enableDepthTest();
    }

    public void renderCarriedItem(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ItemStack itemstack = this.menu.getCarried();
        if (!itemstack.isEmpty()) {
            this.renderFloatingItem(gfx, itemstack, pMouseX - 8, pMouseY - 8);
        }
    }

    private void renderFloatingItem(GuiGraphics gfx, ItemStack pStack, int pX, int pY) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.translate(0.0D, 0.0D, 1000.0D);
        RenderSystem.applyModelViewMatrix();

        var font = IClientItemExtensions.of(pStack).getFont(pStack, IClientItemExtensions.FontContext.ITEM_COUNT);
        if (font == null) font = this.font;

        gfx.renderItem(pStack, pX, pY);
        gfx.renderItemDecorations(font, pStack, pX, pY);
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
     * @param gfx
     * @param mouseX
     * @param mouseY
     */
    protected void renderOver(GuiGraphics gfx, int mouseX, int mouseY) {

    }

    protected void renderContents(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
    }

    protected void drawItem(GuiGraphics gfx, ItemStack stack, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        gfx.pose().translate(0,0, 32);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        var tr = gfx.pose().last().pose().getTranslation(new Vector3f());
        gfx.renderItem(stack, (int)tr.x() + x, (int)tr.y() + y);
        RenderSystem.disableDepthTest();
    }

    protected void textureQuad(GuiGraphics gfx, ResourceLocation location, double x, double y, double width, double height) {
        TorchUtil.textureQuad(gfx, location, x, y, width, height);
    }

    protected void textureQuad(GuiGraphics gfx, ResourceLocation location, double x, double y, double width, double height, float minU, float minV, float maxU, float maxV) {
        TorchUtil.textureQuad(gfx, location, x, y, width, height, minU, minV, maxU, maxV);
    }

    protected void colorQuad(GuiGraphics gfx, int color, double x, double y, double width, double height) {
        TorchUtil.colorQuad(gfx, color, x, y, width, height);
    }

    protected void colorQuad(GuiGraphics gfx, float r, float g, float b, float a, double x, double y, double width, double height) {
        TorchUtil.colorQuad(gfx, r, g, b, a, x, y, width, height);
    }

    public boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key);
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

    public void removeWindow(Window window) {
        windows.remove(window);
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
}
