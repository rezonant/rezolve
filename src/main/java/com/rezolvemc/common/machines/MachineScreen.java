package com.rezolvemc.common.machines;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.inventory.BaseSlot;
import com.rezolvemc.common.inventory.IngredientSlot;
import com.rezolvemc.common.inventory.OutputSlot;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.torchmc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class MachineScreen<MenuT extends MachineMenu> extends AbstractContainerScreen<MenuT> {
    protected MachineScreen(MenuT menu, Inventory playerInventory, Component pTitle, int width, int height) {
        super(menu, playerInventory, pTitle);
        this.imageWidth = width;
        this.imageHeight = height;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    protected boolean enableInventoryLabel = true;
    private boolean initialized = false;
    private boolean wasMoved = false;
    private Panel panel;
    protected VerticalLayoutPanel leftShoulderButtons;
    protected VerticalLayoutPanel rightShoulderButtons;
    private boolean resizing = false;
    private boolean moving = false;
    private ResizeMode resizeMode;
    private double clickMouseX = 0;
    private double clickMouseY = 0;
    private double moveStartLeftPos = 0;
    private double moveStartTopPos = 0;
    private int resizeStartWidth = 0;
    private int resizeStartHeight = 0;

    private enum ResizeMode {
        RIGHT, BOTTOM, BOTTOM_RIGHT
    }

    @Override
    protected void init() {
        if (!initialized) {
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.topPos = (this.height - this.imageHeight) / 2;
            initialized = true;
        }

        addRenderableWidget(leftShoulderButtons = new VerticalLayoutPanel());
        leftShoulderButtons.move(leftPos - IconButton.SIZE - 3, topPos, IconButton.SIZE, imageHeight);
        leftShoulderButtons.setIsDecoration(true);
        leftShoulderButtons.setSpace(2);

        addRenderableWidget(rightShoulderButtons = new VerticalLayoutPanel());
        rightShoulderButtons.move(leftPos + imageWidth + 3, topPos, IconButton.SIZE, imageHeight);
        rightShoulderButtons.setIsDecoration(true);
        rightShoulderButtons.setSpace(2);

        if (menu.hasPlayerInventorySlots())
            addInventoryGrid(menu.getFirstPlayerInventorySlot());
    }

    protected IconButton addLeftShoulderButton(Component label, ResourceLocation icon, Runnable action) {
        return leftShoulderButtons.addChild(new IconButton(0, 0, label, icon, action));
    }

    protected IconButton addRightShoulderButton(Component label, ResourceLocation icon, Runnable action) {
        return rightShoulderButtons.addChild(new IconButton(0, 0, label, icon, action));
    }

    protected <T extends Panel> T setPanel(T panel) {
        if (this.panel != null) {
            removeWidget(this.panel);
        }

        this.panel = panel;
        panel.setParentSize(new Size(imageWidth, imageHeight));
        addRenderableWidget(panel);

        int margin = 8;
        panel.move(margin + leftPos, margin + topPos + font.lineHeight + 2, imageWidth - margin*2, imageHeight - margin*2 - font.lineHeight - 2);
        return panel;
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        enableScissor(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight);

        this.font.draw(pPoseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 4210752);

        if (enableInventoryLabel)
            this.font.draw(pPoseStack, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 4210752);

        disableScissor();
    }

    protected Label addLabel(Component content, int x, int y) {
        return addLabel(content, x, y, 99999);
    }

    protected Label addLabel(Component content, int x, int y, int width) {
        return addRenderableWidget(new Label(font, content, x, y, width));
    }

    protected ProgressIndicator addOperationProgressIndicator(int x, int y) {
        return addProgressIndicator(x, y, Component.translatable("rezolve.screens.progress"), () -> (double)menu.progress);
    }

    protected ProgressIndicator addProgressIndicator(int x, int y, Component label, Supplier<Double> stateProvider) {
        return addRenderableWidget(new ProgressIndicator(x, y, label) {
            @Override
            protected void updateState() {
                super.updateState();
                setValue(stateProvider.get());
            }
        });
    }

    protected void addSlot(Component label, int slotId) {
        addSlotGrid(label, 1, slotId, 1, false);
    }

    protected void addSlot(Component label, int slotId, boolean bottom) {
        addSlotGrid(label, 1, slotId, 1, bottom);
    }

    protected void addSlotGrid(Component label, int breadth, int firstSlotId, int count) {
        addSlotGrid(label, breadth, firstSlotId, count, false);
    }

    protected void addSlotGrid(Component label, int breadth, int firstSlotId, int count, boolean bottom) {
        var labelWidth = font.width(label);
        var slotWidth = 18;
        var gridWidth = breadth * slotWidth;
        var firstSlot = menu.getSlot(firstSlotId);
        var labelX = firstSlot.x;
        var labelY = firstSlot.y - font.lineHeight - 5;

        if (labelWidth < gridWidth) {
            labelX = firstSlot.x + gridWidth / 2 - labelWidth / 2;
        }

        if (bottom) {
            labelY = firstSlot.y + slotWidth + 5;
        }

        addLabel(label, leftPos + labelX, topPos + labelY, labelWidth + 20);

        for (int i = 0, max = count; i < max; ++i) {
            addRenderableWidget(
                new SlotWidget(
                    leftPos,
                    topPos,
                    Component.empty()
                            .append(label)
                            .append(" ")
                            .append(Component.translatable("screens.resolve.slot"))
                            .append(" ")
                            .append((i + 1)+""),
                    menu.getSlot(firstSlotId + i)
                )
            );
        }
    }

    private void addInventoryGrid(int firstSlotId) {
        for (int i = 0, max = 36; i < max; ++i) {
            addRenderableWidget(
                    new SlotWidget(
                            leftPos,
                            topPos,
                            Component.empty()
                                    .append(Component.translatable("screens.resolve.inventory_slot"))
                                    .append(" ")
                                    .append((i + 1)+""),
                            menu.getSlot(firstSlotId + i)
                    )
            );
        }
    }

    protected Meter addMeter(
            Component narrationTitle, Component label, ResourceLocation texture, int x, int y, int height
    ) {
        return addMeter(narrationTitle, label, texture, x, y, height, null);
    }

    protected Meter addMeter(
            Component narrationTitle, Component label, ResourceLocation texture, int x, int y, int height,
            Function<MenuT, Double> stateFunc
    ) {
        var meter = addRenderableWidget(new Meter(font, x, y, height, narrationTitle, label, texture) {
            @Override
            public void updateState() {
                if (stateFunc != null)
                    setValue(stateFunc.apply(menu));
            }
        });

        if (x < 0) {
            meter.move(imageWidth - meter.getWidth(), y);
        }

        return meter;
    }

    protected Meter addEnergyMeter(int x, int y, int height) {
        return addMeter(
                Component.translatable("screens.rezolve.energy_meter"),
                Component.translatable("screens.rezolve.energy_unit"),
                new ResourceLocation("rezolve", "textures/gui/widgets/energy_meter.png"),
                x, y, height,
                menu -> menu.energyStored / (double)menu.energyCapacity
        );
    }

    protected void renderSubWindows(PoseStack poseStack, double mouseX, double mouseY) {

    }

    protected void drawItem(PoseStack poseStack, ItemStack stack, int x, int y) {

        RenderSystem.setShaderColor(1, 1, 1, 1);

        poseStack.translate(0,0, 32);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        //RenderHelper.disableStandardItemLighting();
        //RenderHelper.enableGUIStandardItemLighting();

        var tr = RezolveGuiUtil.getTranslation(poseStack.last().pose());

        this.setBlitOffset(200);
        Font font = this.font;
        this.itemRenderer.renderAndDecorateItem(stack, (int)tr.x() + x, (int)tr.y() + y);
        //this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;

        //RenderHelper.enableStandardItemLighting();
        //GlStateManager.enableLighting();
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

        clickMouseX = pMouseX;
        clickMouseY = pMouseY;

        if (hoveringMenuBar(pMouseX, pMouseY)) {
            moving = true;
            moveStartLeftPos = leftPos;
            moveStartTopPos = topPos;
        } else if (hoveringBottomEdge(pMouseX, pMouseY) && hoveringRightEdge(pMouseX, pMouseY)) {
            resizing = true;
            resizeMode = ResizeMode.BOTTOM_RIGHT;
            resizeStartWidth = imageWidth;
            resizeStartHeight = imageHeight;
            return true;
        } else if (hoveringBottomEdge(pMouseX, pMouseY)) {
            resizing = true;
            resizeMode = ResizeMode.BOTTOM;
            resizeStartWidth = imageWidth;
            resizeStartHeight = imageHeight;
            return true;
        } else if (hoveringRightEdge(pMouseX, pMouseY)) {
            resizing = true;
            resizeMode = ResizeMode.RIGHT;
            resizeStartWidth = imageWidth;
            resizeStartHeight = imageHeight;
            return true;
        }


        Slot slot = this.getSlotUnderMouse();

        if (slot != null) {
            if (slot instanceof IngredientSlot ingredientSlot) {
                menu.setIngredient(ingredientSlot, menu.getCarried());
                return true;
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
        if (getFocused() instanceof EditBox && ((EditBox) getFocused()).isFocused() && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            return false;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        boolean textFocused = false;

        if (!textFocused || pCodePoint != 'e')
            return super.charTyped(pCodePoint, pModifiers);

        return false;
    }

    /**
     * Responsible for updating UI widgets state based on state changes that happen in the Menu.
     */
    public void updateStateFromMenu() {

    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {

        if (resizing) {
            switch (resizeMode) {
                case RIGHT -> {
                    imageWidth = (int)(resizeStartWidth + (pMouseX - clickMouseX));
                }
                case BOTTOM -> {
                    imageHeight = (int)(resizeStartHeight + (pMouseY - clickMouseY));
                }
                case BOTTOM_RIGHT -> {
                    imageWidth = (int)(resizeStartWidth + (pMouseX - clickMouseX));
                    imageHeight = (int)(resizeStartHeight + (pMouseY - clickMouseY));
                }
            }

            if (!wasMoved) {
                leftPos = (width - imageWidth) / 2;
                topPos = (height - imageHeight) / 2;
            }

            rebuildWidgets();

            return true;
        } else if (moving) {
            leftPos = (int)(moveStartLeftPos + (pMouseX - clickMouseX));
            topPos = (int)(moveStartTopPos + (pMouseY - clickMouseY));
            wasMoved = true;

            rebuildWidgets();

            return true;
        }

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
        resizing = false;
        moving = false;

        Slot slot = this.getSlotUnderMouse();

        if (slot != null) {
            if (slot instanceof IngredientSlot) {
                return true;
            }
        }

        boolean wasDragging = isDragging();
        var focusedListener = getFocused();

        boolean result = super.mouseReleased(pMouseX, pMouseY, pButton);

        // Similar (but opposite) to mouseDragged(), we actually *do* need to deliver mouse release events to whatever the
        // dragged widget is, so that they can release their hover state. Seriously, this is basic UI framework stuff, but
        // here we are. We could work around it like we do in mouseDragged(), but Forge, ever helpful, makes AbstractContainerMenu
        // call super. :-\
        //

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

    private int dragHandleSize = 4;

    @Override
    public final void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        updateStateFromMenu();

        // Underlay fade
        //colorQuad(pPoseStack, 0x80000000, 0, 0, width, height);

//        var scissorBorder = 0;
//        enableScissor(
//                leftPos + scissorBorder,
//                topPos + scissorBorder,
//                leftPos + imageWidth - scissorBorder*2,
//                topPos + imageHeight - scissorBorder*2
//        );

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
//        disableScissor();

        pPoseStack.pushPose();
        pPoseStack.translate(leftPos, topPos, 1);
        RenderSystem.applyModelViewMatrix();
        renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
        pPoseStack.popPose();
        RenderSystem.applyModelViewMatrix();

        // Tooltips

        var slot = getSlotUnderMouse();
        List<Component> tooltipContent = null;

        if (slot != null) {
            var hasItem = slot.getItem() != null && !slot.getItem().isEmpty();
            var itemComponent = hasItem ? slot.getItem().getItem().getName(slot.getItem()) : Component.translatable("screens.rezolve.empty");
            tooltipContent = new ArrayList<>();

            if (hasItem)
                tooltipContent.add(itemComponent);

            var isCarrying = menu.getCarried() != null && !menu.getCarried().isEmpty();

            if (slot instanceof BaseSlot baseSlot) {
                if (slot instanceof OutputSlot outputSlot) {
                    if (isCarrying) {
                        tooltipContent.add(Component.translatable("screens.rezolve.cannot_drop_into_output").withStyle(ChatFormatting.ITALIC));
                    }
                }

                tooltipContent.add(baseSlot.getLabel().copy().withStyle(ChatFormatting.GRAY));

                tooltipContent.add(
                        Component.empty()
                        .append(Component.translatable("screens.rezolve.automatable").withStyle(ChatFormatting.GREEN))
                        .append(
                                Component.empty()
                                        .withStyle(ChatFormatting.GRAY)
                                        .append(", ")
                                        .append(Component.translatable("screens.rezolve.all_sides").withStyle(ChatFormatting.GRAY))
                        )
                );

                tooltipContent.add(
                        Component.empty()
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable("screens.rezolve.holds_up_to"))
                                .append(" ")
                                .append(Component.literal(slot.getMaxStackSize() + "").withStyle(ChatFormatting.WHITE))
                                .append(" ")
                                .append(Component.translatable("screens.rezolve.items"))
                );
            }

            if (tooltipContent.size() == 0)
                tooltipContent = null;
        }

        if (tooltipContent != null) {
            renderTooltip(
                    pPoseStack,
                    tooltipContent,
                    Optional.empty(),
                    pMouseX, pMouseY
            );
        }

        if (!resizing) {
            if (hoveringRightEdge(pMouseX, pMouseY) && hoveringBottomEdge(pMouseX, pMouseY)) {
                RezolveGuiUtil.colorQuad(
                        pPoseStack, Color.WHITE.withAlpha(0.5f),
                        getRightEdgeStart(), getBottomEdgeStart(),
                        dragHandleSize, dragHandleSize
                );
            } else if (hoveringRightEdge(pMouseX, pMouseY)) {
                RezolveGuiUtil.colorQuad(
                        pPoseStack, Color.WHITE.withAlpha(0.5f),
                        getRightEdgeStart(), topPos,
                        dragHandleSize, imageHeight
                );
            } else if (hoveringBottomEdge(pMouseX, pMouseY)) {
                RezolveGuiUtil.colorQuad(
                        pPoseStack, Color.WHITE.withAlpha(0.5f),
                        leftPos, getBottomEdgeStart(),
                        imageWidth, dragHandleSize
                );
            }
        }
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

    private int titlebarHeight = 18;

    private boolean hoveringMenuBar(double pMouseX, double pMouseY) {
        return leftPos < pMouseX && pMouseX < leftPos + imageWidth
                && topPos < pMouseY && pMouseY < topPos + titlebarHeight;
    }

    private boolean hoveringBottomEdge(double pMouseX, double pMouseY) {
        return getBottomEdgeStart() < pMouseY && pMouseY < getBottomEdgeStart() + dragHandleSize;
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
    }

    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
    }

    private boolean renderBackground = true;
    protected int twoToneHeight = 120;

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if (!renderBackground)
            return;

        //RenderSystem.disableDepthTest();
        Lighting.setupForFlatItems();

        // Subwindows. These are drawn _beneath_ the background image to give the illuision that it is a subwindow.

        pPoseStack.pushPose();
        pPoseStack.translate(this.leftPos, this.topPos, 0);
        RenderSystem.applyModelViewMatrix();
        this.renderSubWindows(pPoseStack, (double)(pMouseX - this.leftPos), (double)(pMouseY - this.topPos));
        pPoseStack.popPose();

        // Machine UI

        RezolveGuiUtil.insetBox(
                pPoseStack,
                Rezolve.loc("textures/gui/widgets/screen_background.png"),
                leftPos, topPos, imageWidth, imageHeight
        );

        if (twoToneHeight > 0) {
            RezolveGuiUtil.insetBox(
                    pPoseStack,
                    Rezolve.loc("textures/gui/widgets/twotone_background.png"),
                    leftPos, topPos, imageWidth, twoToneHeight
            );
        }

        //renderTitleBar(stack, pPartialTick, pMouseX, pMouseY);
        var border = 2;
        colorQuad(
                pPoseStack, 0xFF999999,
                leftPos + border, topPos + border,
                imageWidth - border*2, titlebarHeight - border*2
        );
    }

    protected void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height) {
        RezolveGuiUtil.textureQuad(stack, location, x, y, width, height);
    }

    protected void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height, float minU, float minV, float maxU, float maxV) {
        RezolveGuiUtil.textureQuad(stack, location, x, y, width, height, minU, minV, maxU, maxV);
    }

    protected void colorQuad(PoseStack stack, int color, double x, double y, double width, double height) {
        RezolveGuiUtil.colorQuad(stack, color, x, y, width, height);
    }

    protected void colorQuad(PoseStack stack, float r, float g, float b, float a, double x, double y, double width, double height) {
        RezolveGuiUtil.colorQuad(stack, r, g, b, a, x, y, width, height);
    }

    public MachineMenu getMachineMenu() {
        return (MachineMenu)getMenu();
    }

    private List<Rect2i> getJeiAreas() {
        return List.of(
            leftShoulderButtons.getScreenDesiredRect(),
            rightShoulderButtons.getScreenDesiredRect()
        );
    }

    public static class Jei implements IGuiContainerHandler<MachineScreen<?>> {
        @Override
        public List<Rect2i> getGuiExtraAreas(MachineScreen<?> containerScreen) {
            return containerScreen.getJeiAreas();
        }
    }
}
