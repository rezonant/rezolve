package com.rezolvemc.thunderbolt.cable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.util.RezolveDirectionUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.torchmc.ui.util.TorchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class ThunderboltEndpointConfigurator extends BlockSideConfigurator {
    private static final int PACKED_LIGHT = 15728880;

    public ThunderboltEndpointConfigurator(Function<Direction, FaceConfiguration> sideAccessor) {
        this.sideAccessor = sideAccessor;
    }

    private static ThunderboltCable thunderboltCable;
    private Function<Direction, FaceConfiguration> sideAccessor;

    @Override
    public void renderSide(GuiGraphics gfx, Direction direction, float size, boolean hovered) {

        if (direction == getHoveredSide()) {
            TorchUtil.colorQuad(gfx, 0x8800FF00, 0, 0, size, size);
        }

        var directionName = RezolveDirectionUtil.friendly(direction);
        var directionWidth = font.width(directionName);
        var padding = 4;

        var cap = sideAccessor.apply(direction);

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
                    gfx, indicators.get(i),
                    size / 2 - indicatorWidth / 2 + transmissionTypeSize * i - transmissionTypeSize / 4,
                    size / 2 - transmissionTypeSize / 2,
                    0,
                    transmissionTypeSize
            );
        }

        if (direction == getHoveredSide()) {

            var labelX = size / 2 - (float) directionWidth / 2;
            var labelY = size - font.lineHeight;

            TorchUtil.colorQuad(
                    gfx,
                    0xFFFFFFFF,
                    labelX - padding,
                    labelY - padding * 2,
                    directionWidth + padding * 2,
                    font.lineHeight + padding * 2
            );

            gfx.drawString(font, directionName, (int)labelX, (int)labelY, 0xFF000000, false);
        }
    }

    private void renderItemIn3D(GuiGraphics gfx, ItemStack stack, double x, double y, double z, double size) {
        gfx.pose().pushPose();
        gfx.pose().translate(x, y, z);
        RenderSystem.applyModelViewMatrix();

        var bufferSource = minecraft.renderBuffers().bufferSource();
        BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);

        gfx.pose().translate(8, 8, 0);
        gfx.pose().scale(1, -1, -0.001f);
        gfx.pose().scale((float) size, (float) size, (float) size);
        gfx.pose().translate(0, 0, -0.001f);
        RenderSystem.applyModelViewMatrix();

        minecraft.getItemRenderer().render(
                stack,
                ItemDisplayContext.GUI, false, gfx.pose(), bufferSource, PACKED_LIGHT, OverlayTexture.NO_OVERLAY,
                bakedmodel
        );

        bufferSource.endBatch();

        gfx.pose().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    // TODO: would be used in the future to render the cable pad preview in 3D
    private void renderModel(PoseStack poseStack, BakedModel model, float pRed, float pGreen, float pBlue, int pPackedLight, int pPackedOverlay) {
        if (thunderboltCable == null)
            thunderboltCable = RezolveRegistry.block(ThunderboltCable.class);

        var bufferSource = minecraft.renderBuffers().bufferSource();
        var buffer = bufferSource.getBuffer(RenderType.solid());
        RandomSource randomsource = RandomSource.create();
        List<BakedQuad> pQuads = model.getQuads(thunderboltCable.defaultBlockState(), Direction.UP, randomsource);

        for (BakedQuad bakedquad : pQuads) {
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
}
