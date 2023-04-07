package com.astronautlabs.mc.rezolve.common.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class RezolveGuiUtil {

    public static void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height) {
        textureQuad(stack, location, x, y, width, height, 0, 0, 1, 1);
    }

    public static void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height, float minU, float minV, float maxU, float maxV) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, location);
        RenderSystem.setShaderColor(1, 1, 1, 1);

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
        float red = ((color >> 24) & 0xFF) / 255.0f;
        float green = ((color >> 16) & 0xFF) / 255.0f;
        float blue = ((color >> 8) & 0xFF) / 255.0f;
        float alpha = (color & 0xFF) / 255.0f;
        colorQuad(stack, red, green, blue, alpha, x, y, width, height);
    }

    public static void colorQuad(PoseStack stack, float r, float g, float b, float a, double x, double y, double width, double height) {
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(stack.last().pose(), (float)x, (float)(y + height), 0.0f)                      .color(r, g, b, a).endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float)x + (float)width, (float)(y + height), 0.0f)    .color(r, g, b, a).endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float)x + (float)width, (float)y, 0.0f)               .color(r, g, b, a).endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float)x, (float)y, 0.0f)                                  .color(r, g, b, a).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
}
