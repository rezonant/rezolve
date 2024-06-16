package com.rezolvemc.thunderbolt.cable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;

public abstract class BlockSideConfigurator extends TorchWidget {
    private static final int PACKED_LIGHT = 15728880;

    public BlockSideConfigurator() {
        super(Component.translatable("screens.rezolve.block_configurator"));

        width = 64;
        height = 64;
    }

    BlockState blockState;
    private float rotationX = 45;
    private float rotationY = -22;
    private boolean blockVisible = true; // debug flag
    private float blockFaceScale = 64;
    private Direction hoveredSide = null;
    private Direction selectedSide = null;
    private double totalDragDistance = 0;
    private boolean rotating = false;

    public boolean isBlockVisible() {
        return blockVisible;
    }

    public void setBlockVisible(boolean blockVisible) {
        this.blockVisible = blockVisible;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
    }

    public abstract void renderSide(GuiGraphics gfx, Direction side, float size, boolean hovered);


    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public void setRotation(float x, float y) {
        this.rotationX = x;
        this.rotationY = y;
    }

    public Direction getHoveredSide() {
        return hoveredSide;
    }

    public Direction getSelectedSide() {
        return selectedSide;
    }

    public void setSelectedSide(Direction selectedSide) {
        this.selectedSide = selectedSide;
        selectedSideDidChange(selectedSide);
    }

    /**
     * Called when the selected side has been changed, either by API or by the builtin click handling.
     * @param selectedSide
     */
    protected void selectedSideDidChange(Direction selectedSide) {

    }

    /**
     * Renders the background behind the block. Defaults to a black rectangle. Override to do something fancy.
     * @param gfx
     * @param mouseX
     * @param mouseY
     * @param partialTick
     */
    protected void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        TorchUtil.colorQuad(gfx, Color.BLACK, 0, 0, width, height);
    }

    @Override
    protected void renderContents(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        updateHoveredSide(pMouseX, pMouseY);

        renderBackground(gfx, pMouseX, pMouseY, pPartialTick);

        if (blockVisible) {
            scissor(gfx, 0, 0, width, height, () -> renderBlock(gfx));
        }
    }

    private int visualSize = 64;

    protected void renderBlock(GuiGraphics gfx) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();

        gfx.pose().pushPose();
        gfx.pose().translate(width / 2, height / 2, 100.0F);
        //gfx.pose().translate(-8.0D, -8.0D, 0.0D);
        gfx.pose().scale(1.0F, -1.0F, 1.0F); // models render with positive Y up
        gfx.pose().scale(visualSize*2, visualSize*2, visualSize*2);
        RenderSystem.applyModelViewMatrix();

        gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.YP, 180));
        gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.XP, rotationY));
        gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.YP, rotationX));

        var bufferSource = minecraft.renderBuffers().bufferSource();

        if (blockVisible && blockState != null) {
            gfx.pose().pushPose();
            gfx.pose().translate(-0.25f, -0.25f, -0.25f);
            gfx.pose().scale(0.5f, 0.5f, 0.5f);
            RenderSystem.applyModelViewMatrix();
            minecraft.getBlockRenderer().renderSingleBlock(
                    blockState, gfx.pose(), bufferSource, PACKED_LIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            gfx.pose().popPose();
            RenderSystem.applyModelViewMatrix();
        }
        bufferSource.endBatch();

        // Side Overlays

        for (var direction : Direction.values()) {
            // Set up rendering on the given side

            gfx.pose().pushPose();

            switch (direction) {
                case DOWN -> {
                    gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.XN, 90));
                }
                case UP -> {
                    gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.XN, -90));
                }
                case NORTH -> {
                    // None, this is the default
                }
                case SOUTH -> {
                    gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.YN, 180));
                }
                case WEST -> {
                    gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.YN, -90));
                }
                case EAST -> {
                    gfx.pose().mulPose(TorchUtil.rotateAround(TorchUtil.YN, 90));
                }
            }

            RenderSystem.applyModelViewMatrix();

            gfx.pose().scale(-1 / 2.0f, -1 / 2.0f, 1 / 2.0f);
            gfx.pose().translate(-0.5, -0.5, -0.51);
            gfx.pose().scale(1 / blockFaceScale, 1 / blockFaceScale, 1);

            // Render overlay
            renderSide(gfx, direction, blockFaceScale, direction == hoveredSide);

            gfx.pose().popPose();
            RenderSystem.applyModelViewMatrix();
        }

        // Cleanup

        gfx.pose().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        totalDragDistance = 0;
        rotating = true;

        return true;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        boolean wasDragged = totalDragDistance >= 1;
        if (!wasDragged && hoveredSide != null)
            setSelectedSide(hoveredSide);

        rotating = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (rotating) {
            rotationX += pDragX;
            rotationY -= pDragY;

            totalDragDistance += Math.abs(pDragX);
            totalDragDistance += Math.abs(pDragY);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

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

    double xInBlockSpace(double mouseX) {
        return (mouseX - (width / 2 - visualSize / 2)) / visualSize;
    }

    double yInBlockSpace(double mouseY) {
        return (mouseY - (height / 2 - visualSize / 2)) / visualSize;
    }
}
