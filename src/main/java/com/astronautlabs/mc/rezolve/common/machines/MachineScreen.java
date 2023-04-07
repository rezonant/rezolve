package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.common.gui.Label;
import com.astronautlabs.mc.rezolve.common.gui.Meter;
import com.astronautlabs.mc.rezolve.common.gui.RezolveGuiUtil;
import com.astronautlabs.mc.rezolve.common.gui.SlotWidget;
import com.astronautlabs.mc.rezolve.common.inventory.BaseSlot;
import com.astronautlabs.mc.rezolve.common.inventory.IngredientSlot;
import com.astronautlabs.mc.rezolve.common.inventory.OutputSlot;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MachineScreen<MenuT extends MachineMenu> extends AbstractContainerScreen<MenuT> {
    protected MachineScreen(MenuT menu, Inventory playerInventory, Component pTitle, String guiBackgroundResource, int width, int height) {
        super(menu, playerInventory, pTitle);
        this.imageWidth = width;
        this.imageHeight = height;
        this.guiBackgroundResource = guiBackgroundResource;
    }

    int windowWidth;
    int windowHeight;

    protected String guiBackgroundResource;
    private ArrayList<GuiComponent> controls = new ArrayList<>();

    @Override
    protected void init() {
        super.init();
        this.controls.clear();

        if (menu.hasPlayerInventorySlots())
            addInventoryGrid(menu.getFirstPlayerInventorySlot());
    }

    protected Label addLabel(Component content, int x, int y) {
        return addLabel(content, x, y, 99999);
    }

    protected Label addLabel(Component content, int x, int y, int width) {
        return addRenderableWidget(new Label(font, content, x, y, width));
    }

    protected void addSlot(Component label, int slotId) {
        addSlotGrid(label, 1, slotId, 1);
    }

    protected void addSlotGrid(Component label, int breadth, int firstSlotId, int count) {
        var labelWidth = font.width(label);
        var slotWidth = 18;
        var gridWidth = breadth * slotWidth;
        var firstSlot = menu.getSlot(firstSlotId);
        var labelX = firstSlot.x;
        var labelY = firstSlot.y - font.lineHeight - 5;

        if (labelWidth < gridWidth) {
            labelX = firstSlot.x + gridWidth / 2 - labelWidth / 2;
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

        this.setBlitOffset(200);
        Font font = this.font;
        this.itemRenderer.renderAndDecorateItem(stack, leftPos + x, topPos + y);
        //this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;

        //RenderHelper.enableStandardItemLighting();
        //GlStateManager.enableLighting();
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        Slot slot = this.getSlotUnderMouse();

        if (slot != null) {
            if (slot instanceof IngredientSlot) {
                return true;
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (GuiComponent control : this.controls) {
            if (control instanceof EditBox) {
                EditBox textField = (EditBox) control;
                textField.mouseClicked(pMouseX, pMouseY, pButton);
            }
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

        for (GuiComponent control : this.controls) {
            if (control instanceof EditBox) {
                EditBox textField = (EditBox) control;
                if (textField.isFocused()) {
                    textFocused = true;
                    textField.charTyped(pCodePoint, pModifiers);
                    break;
                }
            }
        }

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
    public final void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        updateStateFromMenu();

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        pPoseStack.pushPose();
        pPoseStack.translate(leftPos, topPos, 1);
        RenderSystem.applyModelViewMatrix();
        renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
        pPoseStack.popPose();
        RenderSystem.applyModelViewMatrix();
//        for (GuiComponent control : this.controls) {
//            if (control instanceof GuiTextField) {
//                ((GuiTextField)control).drawTextBox();
//            } else if (control instanceof GuiButton) {
//                ((GuiButton)control).drawButton(this.mc, mouseX, mouseY);
//            }
//        }

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
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
    }

    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
    }

    private boolean renderBackground = true;
    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if (!renderBackground)
            return;

        //RenderSystem.disableDepthTest();
        Lighting.setupForFlatItems();

        // Underlay fade

        colorQuad(pPoseStack, 0x00000080, 0, 0, width, height);

        // Subwindows. These are drawn _beneath_ the background image to give the illuision that it is a subwindow.

        pPoseStack.pushPose();
        pPoseStack.translate(this.leftPos, this.topPos, 0);
        RenderSystem.applyModelViewMatrix();
        this.renderSubWindows(pPoseStack, (double)(pMouseX - this.leftPos), (double)(pMouseY - this.topPos));
        pPoseStack.popPose();

        // Machine UI

        textureQuad(
                pPoseStack,
            new ResourceLocation(this.guiBackgroundResource),
                leftPos, topPos, imageWidth, imageHeight,
                0, 0,
                imageWidth / (float)backgroundTextureWidth,
                imageHeight / (float)backgroundTextureHeight
        );
    }

    protected int backgroundTextureWidth = 256;
    protected int backgroundTextureHeight = 256;

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
}
