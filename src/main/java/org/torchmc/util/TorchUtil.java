package org.torchmc.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.nio.FloatBuffer;

public class TorchUtil {

    public static void drawItem(PoseStack poseStack, ItemStack stack, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.translate(0,0, 32);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        var tr = TorchUtil.getTranslation(poseStack.last().pose());
        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        itemRenderer.renderAndDecorateItem(stack, (int)tr.x() + x, (int)tr.y() + y);
        RenderSystem.disableDepthTest();
    }

    public static void textureQuad(PoseStack stack, ResourceLocation location, double x, double y, double width, double height) {
        textureQuad(stack, location, x, y, width, height, 0, 0, 1, 1);
    }

    public static Vector3f getTranslation(Matrix4f pose) {
        var buf = FloatBuffer.allocate(16);
        pose.store(buf);

        var tx = (int)buf.get(12);
        var ty = (int)buf.get(13);
        var tz = (int)buf.get(14);

        return new Vector3f(tx, ty, tz);
    }

    public static void colorOutline(PoseStack stack, int color, double stroke, double x, double y, double width, double height) {
        colorOutline(stack, Color.argb(color), stroke, x, y, width, height);
    }

    public static void colorOutline(PoseStack stack, Color color, double stroke, double x, double y, double width, double height) {
        colorQuad(stack, color, x - stroke/2, y - stroke / 2, width + stroke, stroke);
        colorQuad(stack, color, x - stroke/2, y + height - stroke / 2, width + stroke, stroke);
        colorQuad(stack, color, x - stroke / 2, y, stroke, height);
        colorQuad(stack, color, x + width - stroke / 2, y, stroke, height);
    }

    public static void insetBox(PoseStack stack, ResourceLocation texture, double x, double y, double width, double height) {

        textureQuad(stack, texture, x, y, 16, 16, 0, 0, 0.5f, 0.5f); // top/left
        textureQuad(stack, texture, x + width - 16, y, 16, 16, 0.5f, 0, 1, 0.5f); // top/right
        textureQuad(stack, texture, x, y + height - 16, 16, 16, 0, 0.5f, 0.5f, 1f); // bottom/left
        textureQuad(stack, texture, x + width - 16, y + height - 16, 16, 16, 0.5f, 0.5f, 1, 1f); // bottom/right


        textureQuad(stack, texture, x + 16, y, width - 32, 16, 8/32.0f, 0, 24/32.0f, 0.5f); // top line
        textureQuad(stack, texture, x + 16, y + height - 16, width - 32, 16, 8/32.0f, 0.5f, 24/32.0f, 1); // bottom line
        textureQuad(stack, texture, x, y + 16, 16, height - 32, 0, 8/32.0f, 0.5f, 24/32.0f); // left line
        textureQuad(stack, texture, x + width - 16, y + 16, 16, height - 32, 0.5f, 8/32.0f, 1, 24/32.0f); // right line

        textureQuad(stack, texture, x + 16, y + 16, width - 32, height - 32, 8/32.0f, 8/32.0f, 24/32.0f, 24/32.0f);
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
        rotatedTextureQuad(stack, location, color, x, y, width, height, minU, minV, maxU, maxV, 0);
    }

    public static void rotatedTextureQuad(
            PoseStack stack, ResourceLocation location,
            Color color,
            double x, double y,
            double width, double height,
            float minU, float minV,
            float maxU, float maxV,
            int rotation
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, location);
        color.applyToShader();

        float[] u = { minU, maxU, maxU, minU };
        float[] v = { maxV, maxV, minV, minV };


        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(stack.last().pose(), (float)x, (float) (y + height), 0.0f)
                .uv(u[(rotation + 0) % 4], v[(rotation + 0) % 4])
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float) (x + width), (float) (y + height), 0.0F)
                .uv(u[(rotation + 1) % 4], v[(rotation + 1) % 4])
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float) (x + width), (float) y, 0.0F)
                .uv(u[(rotation + 2) % 4], v[(rotation + 2) % 4])
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(stack.last().pose(), (float) x, (float) y, 0.0F)
                .uv(u[(rotation + 3) % 4], v[(rotation + 3) % 4])
                .uv(u[3], v[3])
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
        //RenderSystem.disableDepthTest();
        //RenderSystem.colorMask(true, true, true, false);
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
        //RenderSystem.enableDepthTest();
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }
}
