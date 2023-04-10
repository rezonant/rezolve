package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import com.astronautlabs.mc.rezolve.common.gui.Label;
import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThunderboltCableScreen extends MachineScreen<ThunderboltCableMenu> {
    public ThunderboltCableScreen(ThunderboltCableMenu menu, Inventory playerInventory, Component pTitle) {
        super(menu, playerInventory, pTitle, "rezolve:textures/gui/container/thunderbolt_cable_gui.png", 256, 256);

        inventoryLabelX = 46;
        inventoryLabelY = 156;
    }

    private static int transmissionTypesX = 150;
    private static int transmissionTypesY = 145;
    private int nextTransmissionTypeY = transmissionTypesY;
    private static int transmissionTypeHeight = 20;
    private static int transmissionTypeWidth = 100;
    private static int transmissionTypeGap = 4;

    private static final Component SELECT_A_SIDE = Component.translatable("rezolve.thunderbolt.select_a_side");

    Label sideInfoLbl;
    Map<TransmissionType, CycleButton<TransmissionMode>> transmissionTypeButtons = new HashMap<>();

    float rotationX = 45;
    float rotationY = -22;

    boolean rotationInitialized = false;

    @Override
    protected void init() {
        super.init();

        sideInfoLbl = addLabel(SELECT_A_SIDE, leftPos + 10, topPos + 150, 145);

        createTransmissionTypeButtons();
        selectSide(selectedSide);
    }

    private void createTransmissionTypeButtons() {
        nextTransmissionTypeY = transmissionTypesY;
        transmissionTypeButtons.clear();

        for (var type : TransmissionType.values())
            addTransmissionType(type);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (!rotationInitialized && menu.position != null) {
            var vec = minecraft.player.position().subtract(Vec3.atCenterOf(menu.position)).multiply(1, -1, 1);
            var xRot = -angle2d(new Vec2((float)vec.x, (float)vec.z), Vec2.UNIT_X) + 90;
            //var yRot = angle2d(new Vec2((float)vec.x, (float)vec.y), Vec2.UNIT_X) + 90;

            rotationX = (float)xRot;
            //rotationY = (float)yRot;
            rotationInitialized = true;
        }

        if (selectedSide != null) {
            for (var entry : transmissionTypeButtons.entrySet()) {
                var faceConfig = menu.configuration.getFace(selectedSide);
                var transmitConfig = faceConfig.getTransmissionConfiguration(entry.getKey());

                entry.getValue().active = transmitConfig.isSupported();
                entry.getValue().setAlpha(transmitConfig.isSupported() ? 1 : 0.5f);
                entry.getValue().setValue(transmitConfig.getMode());
            }
        }
    }

    private double angle2d(Vec2 a, Vec2 b) {
        var det = a.x * b.y - a.y * b.x;
        return Math.atan2(det, a.dot(Vec2.UNIT_X)) * (180 / Math.PI);
    }

    private void addTransmissionType(TransmissionType type) {
        CycleButton<TransmissionMode> button;
        transmissionTypeButtons.put(type, button = new CycleButton
                .Builder<TransmissionMode>(mode -> mode.translation())
                .withValues(TransmissionMode.values())
                .create(
                        leftPos + transmissionTypesX, topPos + nextTransmissionTypeY,
                        transmissionTypeWidth, transmissionTypeHeight,
                        type.translation(),
                        (btn, mode) -> {
                            menu.setTransmissionMode(selectedSide, type, mode);
                        }
                )
        );
        nextTransmissionTypeY += transmissionTypeHeight + transmissionTypeGap;
        addRenderableWidget(button);
    }

    double xInBlockSpace(double x) {
        return (x - leftPos - itemX) / 64 + 0.5;
    }

    double yInBlockSpace(double y) {
        return (y - topPos - itemY) / 64 + 0.5;
    }

    private Direction hoveredSide = null;
    private Direction selectedSide = null;

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        //this.font.draw(pPoseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    private void updateHoveredSide(double pMouseX, double pMouseY) {

        var shape = Shapes.block();
        var xPos = xInBlockSpace(pMouseX);
        var yPos = yInBlockSpace(pMouseY);
        var degToRad = (float)(Math.PI / 180F);

        var startVec = new Vec3(xPos, yPos, -20)
                .subtract(0.5, 0.5, 0.5)
                .xRot(-rotationY * degToRad)
                .yRot(rotationX * degToRad)
                .add(0.5, 0.5, 0.5)
                ;
        var endVec = new Vec3(xPos, yPos, 20)
                .subtract(0.5, 0.5, 0.5)
                .xRot(-rotationY * degToRad)
                .yRot(rotationX * degToRad)
                .add(0.5, 0.5, 0.5)
                ;
        var hit = false;

        var hitResult = shape.clip(startVec, endVec, BlockPos.ZERO);

        if (!mouseDown) {
            if (hitResult != null) {
                var dir = hitResult.getDirection();

                // This is fine! Nothing to see here!
                if (dir.getStepY() != 0 || dir.getStepZ() == 0)
                    dir = dir.getOpposite();
                hoveredSide = dir;
            } else {
                hoveredSide = null;
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        mouseDown = true;
        totalDragDistance = 0;
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void selectSide(Direction side) {
        selectedSide = hoveredSide;
        if (selectedSide != null) {
            sideInfoLbl.setContent(
                    Component
                            .empty()
                            .append(friendlyDirectionName(selectedSide)).append("\n")
                            .append("\n")
                            .append("Here's some ")
                            .append(Component.literal("cool").withStyle(ChatFormatting.ITALIC))
                            .append(" stuff")
            );
            for (var btn : transmissionTypeButtons.values())
                btn.visible = true;
        } else {
            sideInfoLbl.setContent(SELECT_A_SIDE);
            for (var btn : transmissionTypeButtons.values())
                btn.visible = false;
        }
    }

    private double totalDragDistance = 0;
    private boolean mouseDown = false;

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        mouseDown = false;
        boolean wasDragged = totalDragDistance >= 1;
        if (!wasDragged && hoveredSide != null)
            selectSide(hoveredSide);

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        rotationX += pDragX;
        rotationY -= pDragY;

        totalDragDistance += Math.abs(pDragX);
        totalDragDistance += Math.abs(pDragY);

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

    }

    int itemX = 128;
    int itemY = 80;

    private static final int PACKED_LIGHT = 15728880;
    private static BakedModel PLATE_PREVIEW;
    private boolean enableBlockRendering = true; // debug flag

    float blockFaceScale = 64;

    @Override
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);

        updateHoveredSide(pMouseX, pMouseY);

        if (menu.direction == null)
            return;

        var blockState = getTargetBlockState();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();

        pPoseStack.pushPose();
        pPoseStack.translate(imageWidth / 2, 80, 100.0F);
        //pPoseStack.translate(-8.0D, -8.0D, 0.0D);
        pPoseStack.scale(1.0F, -1.0F, 1.0F); // models render with positive Y up
        pPoseStack.scale(128, 128, 128);
        RenderSystem.applyModelViewMatrix();

        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180));
        pPoseStack.mulPose(Vector3f.XP.rotationDegrees(rotationY));
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(rotationX));

        var bufferSource = minecraft.renderBuffers().bufferSource();

        if (enableBlockRendering) {
            pPoseStack.pushPose();
            pPoseStack.translate(-0.25f, -0.25f, -0.25f);
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            RenderSystem.applyModelViewMatrix();
            minecraft.getBlockRenderer().renderSingleBlock(
                    blockState, pPoseStack, bufferSource, PACKED_LIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            pPoseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        bufferSource.endBatch();

        // Overlays

        for (var direction : Direction.values()) {
            // Set up rendering on the given side

            pPoseStack.pushPose();

            switch (direction) {
                case DOWN -> {
                    pPoseStack.mulPose(Vector3f.XN.rotationDegrees(90));
                }
                case UP -> {
                    pPoseStack.mulPose(Vector3f.XN.rotationDegrees(-90));
                }
                case NORTH -> {
                    // None, this is the default
                }
                case SOUTH -> {
                    pPoseStack.mulPose(Vector3f.YN.rotationDegrees(180));
                }
                case WEST -> {
                    pPoseStack.mulPose(Vector3f.YN.rotationDegrees(-90));
                }
                case EAST -> {
                    pPoseStack.mulPose(Vector3f.YN.rotationDegrees(90));
                }
            }

            RenderSystem.applyModelViewMatrix();

            // Render overlay
            pPoseStack.scale(-1 / 2.0f, -1 / 2.0f, 1 / 2.0f);
            pPoseStack.translate(-0.5, -0.5, -0.51);
            pPoseStack.scale(1 / blockFaceScale, 1 / blockFaceScale, 1);

            if (direction == hoveredSide) {
                colorQuad(pPoseStack, 0x8800FF00, 0, 0, blockFaceScale, blockFaceScale);
            }

            var directionName = friendlyDirectionName(direction);
            var directionWidth = font.width(directionName);
            var padding = 4;

            var cap = menu.configuration.getFace(direction);

            var transmissionTypeSize = 12;
            List<ItemStack> indicators = new ArrayList<>();

            if (cap.getTransmissionConfiguration(TransmissionType.ITEMS).getMode() != TransmissionMode.NONE) {
                indicators.add(new ItemStack(Items.CHEST));
            }

            if (cap.getTransmissionConfiguration(TransmissionType.FLUIDS).getMode() != TransmissionMode.NONE) {
                indicators.add(new ItemStack(Items.BUCKET));
            }

            if (cap.getTransmissionConfiguration(TransmissionType.ENERGY).getMode() != TransmissionMode.NONE) {
                indicators.add(new ItemStack(Items.REDSTONE_LAMP));
            }

            var indicatorWidth = transmissionTypeSize * indicators.size();
            for (int i = 0, max = indicators.size(); i < max; ++i) {
                renderItemIn3D(
                        pPoseStack, indicators.get(i),
                        blockFaceScale / 2 - indicatorWidth / 2 + transmissionTypeSize * i - transmissionTypeSize / 4,
                        blockFaceScale / 2 - transmissionTypeSize / 2,
                        0,
                        transmissionTypeSize
                );
            }

            if (direction == hoveredSide) {

                var labelX = blockFaceScale / 2 - (float)directionWidth / 2;
                var labelY = blockFaceScale - font.lineHeight; // blockFaceScale / 2 - font.lineHeight / 2;
//                if (direction.getStepY() != 0) {
//                    labelY = blockFaceScale / 2 - font.lineHeight / 2;
//                }

                colorQuad(
                        pPoseStack,
                        0xFFFFFFFF,
                        labelX - padding,
                        labelY - padding * 2,
                        directionWidth + padding*2,
                        font.lineHeight + padding*2
                );
                font.draw(pPoseStack, directionName, labelX, labelY, 0xFF000000);
            }


            pPoseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        // Cleanup

        pPoseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private ItemTransforms.TransformType indicatorTransform = ItemTransforms.TransformType.GUI;
    private float compressionFML = -0.001f;

    private void renderItemIn3D(PoseStack poseStack, ItemStack stack, double x, double y, double z, double size) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        RenderSystem.applyModelViewMatrix();

        var bufferSource = minecraft.renderBuffers().bufferSource();
        BakedModel bakedmodel = this.itemRenderer.getModel(stack, minecraft.level, null, 0);

//        RenderSystem.disableDepthTest();
//        Lighting.setupForFlatItems();
//        renderGuiItem(poseStack, stack, (int)x, (int)y, bakedmodel);
//        RenderSystem.enableDepthTest();

        poseStack.translate(8, 8, 0);
        poseStack.scale(1, -1, compressionFML);
        poseStack.scale((float)size, (float)size, (float)size);
        poseStack.translate(0, 0, -0.001f);
        RenderSystem.applyModelViewMatrix();

//        minecraft.getItemRenderer().renderModelLists(
//                bakedmodel, stack, PACKED_LIGHT, OverlayTexture.NO_OVERLAY, poseStack,
//                bufferSource.getBuffer(RenderType.solid())
//        );

        minecraft.getItemRenderer().render(stack, indicatorTransform, false, poseStack, bufferSource, PACKED_LIGHT, OverlayTexture.NO_OVERLAY,
                bakedmodel
        );

//        minecraft.getItemRenderer().renderStatic(
//                stack,
//                indicatorTransform,
//                PACKED_LIGHT, OverlayTexture.NO_OVERLAY,
//                poseStack, bufferSource, 0
//        );

        bufferSource.endBatch();

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void renderGuiItem(PoseStack poseStack, ItemStack pStack, int pX, int pY, BakedModel pBakedModel) {
        minecraft.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(
                 0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)pX, (double)pY, (double)(100.0F + minecraft.getItemRenderer().blitOffset));
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !pBakedModel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        minecraft.getItemRenderer().render(
                pStack, ItemTransforms.TransformType.GUI, false,
                poseStack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY,
                pBakedModel
        );

        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private Component friendlyDirectionName(Direction direction) {
        return Component.translatable("rezolve.directions." + direction.getName());
    }

    private static ThunderboltCable thunderboltCable;
    private void renderModel(PoseStack poseStack, BakedModel model, float pRed, float pGreen, float pBlue, int pPackedLight, int pPackedOverlay) {
        if (thunderboltCable == null)
            thunderboltCable = RezolveRegistry.block(ThunderboltCable.class);

        var bufferSource = minecraft.renderBuffers().bufferSource();
        var buffer = bufferSource.getBuffer(RenderType.solid());
        RandomSource randomsource = RandomSource.create();
        List<BakedQuad> pQuads = model.getQuads(thunderboltCable.defaultBlockState(), Direction.UP, randomsource);

        for(BakedQuad bakedquad : pQuads) {
            float f;
            float f1;
            float f2;
            if (bakedquad.isTinted()) {
                f = Mth.clamp(pRed, 0.0F, 1.0F);
                f1 = Mth.clamp(pGreen, 0.0F, 1.0F);
                f2 = Mth.clamp(pBlue, 0.0F, 1.0F);
            } else {
                f = 1.0F;
                f1 = 1.0F;
                f2 = 1.0F;
            }

            buffer.putBulkData(poseStack.last(), bakedquad, f, f1, f2, pPackedLight, pPackedOverlay);
        }

        bufferSource.endBatch();
    }

    @NotNull
    private BlockState getTargetBlockState() {
        return Block.stateById(menu.targetBlockId);
    }
}
