package com.astronautlabs.mc.rezolve.common.gui;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class RezolveGuiUtil {

    public static void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height) {
        textureQuad(stack, location, x, y, width, height, 0, 0, 1, 1);
    }

    public static void insetBox(PoseStack stack, double x, double y, double width, double height) {
        var bg = RezolveMod.loc("textures/gui/widgets/storage_view_background.png");

        textureQuad(stack, bg, x, y, width, 1, 0, 0, 1, 1/32.0f); // top line
        textureQuad(stack, bg, x, y + height, width, 1, 0, 31.0f/32.0f, 1, 1); // bottom line
        textureQuad(stack, bg, x, y, 1, height, 0, 0, 1/32.0f, 1); // left line
        textureQuad(stack, bg, x + width - 1, y, 1, height, 31/32.0f, 0, 1, 1); // right line

        textureQuad(stack, bg, x + 1, y + 1, width - 2, height - 2, 1/32.0f, 1/32.0f, 31/32.0f, 31/32.0f);

    }

    public static void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height, float minU, float minV, float maxU, float maxV) {
        textureQuad(stack, location, Color.WHITE, x, y, width, height, minU, minV, maxU, maxV);
    }

    public static void textureQuad(
            PoseStack stack, ResourceLocation location,
            Color color,
            double x, double y,
            double width, double height,
            float minU, float minV,
            float maxU, float maxV
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, location);
        color.applyToShader();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(stack.last().pose(), (float)x, (float) (y + height), 0.0f)
                .uv(minU, maxV)
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float) (x + width), (float) (y + height), 0.0F)
                .uv(maxU, maxV)
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float) (x + width), (float) y, 0.0F)
                .uv(maxU, minV)
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float) x, (float) y, 0.0F)
                .uv(minU, minV)
                .color(255, 255, 255, 255)
                .endVertex();

        tesselator.end();
    }

    public static void colorQuad(PoseStack stack, int color, double x, double y, double width, double height) {
        colorQuad(stack, Color.argb(color), x, y, width, height);
    }

    public static void colorQuad(PoseStack stack, float r, float g, float b, float a, double x, double y, double width, double height) {
        colorQuad(stack, Color.of(r, g, b, a), x, y, width, height);
    }

    public static void colorQuad(PoseStack stack, Color color, double x, double y, double width, double height) {
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferbuilder.vertex(stack.last().pose(), (float)x, (float)(y + height), 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();

        bufferbuilder.vertex(stack.last().pose(), (float)x + (float)width, (float)(y + height), 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();

        bufferbuilder.vertex(stack.last().pose(), (float)x + (float)width, (float)y, 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();

        bufferbuilder.vertex(stack.last().pose(), (float)x, (float)y, 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();


        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
}
