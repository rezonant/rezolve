package com.rezolvemc.common.machines;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.inventory.BaseSlot;
import com.rezolvemc.common.inventory.IngredientSlot;
import com.rezolvemc.common.inventory.OutputSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.torchmc.TorchScreen;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.widgets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class MachineScreen<MenuT extends MachineMenu> extends TorchScreen<MenuT> {
    protected MachineScreen(MenuT menu, Inventory playerInventory, Component pTitle, int width, int height) {
        super(menu, playerInventory, pTitle, width, height);
    }

    protected VerticalLayoutPanel leftShoulderButtons;
    protected VerticalLayoutPanel rightShoulderButtons;

    @Override
    protected void setup() {
        getMainWindow().addChild(leftShoulderButtons = new VerticalLayoutPanel());
        leftShoulderButtons.setIsDecoration(true);
        leftShoulderButtons.setSpace(2);

        getMainWindow().addChild(rightShoulderButtons = new VerticalLayoutPanel());
        rightShoulderButtons.setIsDecoration(true);
        rightShoulderButtons.setSpace(2);

        applyDimensions();

//        if (menu.hasPlayerInventorySlots())
//            addInventoryGrid(menu.getFirstPlayerInventorySlot());
    }

    @Override
    protected void applyDimensions() {
        super.applyDimensions();

        if (leftShoulderButtons != null)
            leftShoulderButtons.move(- IconButton.SIZE - 3, 0, IconButton.SIZE, imageHeight);

        if (rightShoulderButtons != null)
            rightShoulderButtons.move(imageWidth + 3, 0, IconButton.SIZE, imageHeight);
    }

    protected IconButton addLeftShoulderButton(String label, ResourceLocation icon) {
        return addLeftShoulderButton(Component.literal(label), icon);
    }

    protected IconButton addLeftShoulderButton(Component label, ResourceLocation icon) {
        return leftShoulderButtons.addChild(new IconButton(label, icon));
    }

    protected IconButton addRightShoulderButton(Component label, ResourceLocation icon) {
        return rightShoulderButtons.addChild(new IconButton(label, icon));
    }

    protected IconButton addRightShoulderButton(String label, ResourceLocation icon) {
        return addRightShoulderButton(Component.literal(label), icon);
    }

    protected Label addLabel(Component content, int x, int y) {
        return addLabel(content, x, y, 99999);
    }

    protected Label addLabel(Component content, int x, int y, int width) {
        return addChild(new Label(content), label -> {
            label.move(x, y, width, font.lineHeight);
        });
    }

    @Deprecated
    protected ProgressIndicator addOperationProgressIndicator(int x, int y) {
        return addProgressIndicator(x, y, Component.translatable("rezolve.screens.progress"), () -> (double)menu.progress);
    }

    @Deprecated
    protected ProgressIndicator addProgressIndicator(int x, int y, Component label, Supplier<Double> stateProvider) {
        return addRenderableWidget(new ProgressIndicator(label) {
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

        var meter = addChild(new Meter(narrationTitle, label, texture) {
            @Override
            public void updateState() {
                if (stateFunc != null)
                    setValue(stateFunc.apply(menu));
            }
        }, widget -> widget.move(x, y, widget.getWidth(), height));

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

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
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
    protected void renderOver(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderOver(poseStack, mouseX, mouseY);
        renderTooltips(poseStack, mouseX, mouseY);
    }

    protected void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {

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

                if (baseSlot.getLabel() != null)
                    tooltipContent.add(baseSlot.getLabel().copy().withStyle(ChatFormatting.GRAY));

                if (baseSlot.getHint() != null) {
                    var content = baseSlot.getHint().getString().split("\n");
                    for (var line : content) {
                        tooltipContent.add(Component.literal(line).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
                    }
                }

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
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        pPoseStack.pushPose();
        pPoseStack.translate(this.leftPos, this.topPos, 0);
        RenderSystem.applyModelViewMatrix();
        this.renderSubWindows(pPoseStack, (double)(pMouseX - this.leftPos), (double)(pMouseY - this.topPos));
        pPoseStack.popPose();
    }

    public MachineMenu getMachineMenu() {
        return (MachineMenu)getMenu();
    }

    @Override
    protected List<Rect2i> getJeiAreas() {
        var list = super.getJeiAreas();

        if (leftShoulderButtons != null)
            list.add(leftShoulderButtons.getDesiredScreenRect());

        if (rightShoulderButtons != null)
            list.add(rightShoulderButtons.getDesiredScreenRect());

        return list;
    }
}
