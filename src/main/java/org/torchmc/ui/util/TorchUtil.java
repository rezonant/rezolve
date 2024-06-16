package org.torchmc.ui.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

public class TorchUtil {

    public static void drawItem(GuiGraphics gfx, ItemStack stack, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        //gfx.pose().translate(0,0, 32);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        var tr = TorchUtil.getTranslation(gfx.pose().last().pose());
        gfx.renderItem(stack, x, y);
        RenderSystem.disableDepthTest();
    }

    public static void textureQuad(GuiGraphics gfx, ResourceLocation location, double x, double y, double width, double height) {
        textureQuad(gfx, location, x, y, width, height, 0, 0, 1, 1);
    }

    /**
     * @deprecated use pose.getTranslation()
     */
    @Deprecated
    public static Vector3f getTranslation(Matrix4f pose) {
        return pose.getTranslation(new Vector3f());
    }

    public static void colorOutline(GuiGraphics gfx, int color, double stroke, double x, double y, double width, double height) {
        colorOutline(gfx, Color.argb(color), stroke, x, y, width, height);
    }

    public static void colorOutline(GuiGraphics gfx, Color color, double stroke, double x, double y, double width, double height) {
        colorQuad(gfx, color, x - stroke/2, y - stroke / 2, width + stroke, stroke);
        colorQuad(gfx, color, x - stroke/2, y + height - stroke / 2, width + stroke, stroke);
        colorQuad(gfx, color, x - stroke / 2, y, stroke, height);
        colorQuad(gfx, color, x + width - stroke / 2, y, stroke, height);
    }

    public static void insetBox(GuiGraphics gfx, ResourceLocation texture, double x, double y, double width, double height) {
        var stack = gfx.pose();

        textureQuad(gfx, texture, x, y, 16, 16, 0, 0, 0.5f, 0.5f); // top/left
        textureQuad(gfx, texture, x + width - 16, y, 16, 16, 0.5f, 0, 1, 0.5f); // top/right
        textureQuad(gfx, texture, x, y + height - 16, 16, 16, 0, 0.5f, 0.5f, 1f); // bottom/left
        textureQuad(gfx, texture, x + width - 16, y + height - 16, 16, 16, 0.5f, 0.5f, 1, 1f); // bottom/right


        textureQuad(gfx, texture, x + 16, y, width - 32, 16, 8/32.0f, 0, 24/32.0f, 0.5f); // top line
        textureQuad(gfx, texture, x + 16, y + height - 16, width - 32, 16, 8/32.0f, 0.5f, 24/32.0f, 1); // bottom line
        textureQuad(gfx, texture, x, y + 16, 16, height - 32, 0, 8/32.0f, 0.5f, 24/32.0f); // left line
        textureQuad(gfx, texture, x + width - 16, y + 16, 16, height - 32, 0.5f, 8/32.0f, 1, 24/32.0f); // right line

        textureQuad(gfx, texture, x + 16, y + 16, width - 32, height - 32, 8/32.0f, 8/32.0f, 24/32.0f, 24/32.0f);
    }

    public static void textureQuad(GuiGraphics gfx, ResourceLocation location, double x, double y, double width, double height, float minU, float minV, float maxU, float maxV) {
        textureQuad(gfx, location, Color.WHITE, x, y, width, height, minU, minV, maxU, maxV);
    }

    public static void textureQuad(
            GuiGraphics gfx,
            ResourceLocation location,
            Color color,
            double x, double y,
            double width, double height,
            float minU, float minV,
            float maxU, float maxV
    ) {
        rotatedTextureQuad(gfx, location, color, x, y, width, height, minU, minV, maxU, maxV, 0);
    }

    public static void rotatedTextureQuad(
            GuiGraphics gfx,
            ResourceLocation location,
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
        bufferbuilder.vertex(gfx.pose().last().pose(), (float)x, (float) (y + height), 0.0f)
                .uv(u[(rotation + 0) % 4], v[(rotation + 0) % 4])
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(gfx.pose().last().pose(), (float) (x + width), (float) (y + height), 0.0F)
                .uv(u[(rotation + 1) % 4], v[(rotation + 1) % 4])
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(gfx.pose().last().pose(), (float) (x + width), (float) y, 0.0F)
                .uv(u[(rotation + 2) % 4], v[(rotation + 2) % 4])
                .color(255, 255, 255, 255)
                .endVertex();
        bufferbuilder.vertex(gfx.pose().last().pose(), (float) x, (float) y, 0.0F)
                .uv(u[(rotation + 3) % 4], v[(rotation + 3) % 4])
                .uv(u[3], v[3])
                .color(255, 255, 255, 255)
                .endVertex();

        tesselator.end();
        Color.WHITE.applyToShader();
    }

    public static void colorQuad(GuiGraphics gfx, int color, double x, double y, double width, double height) {
        colorQuad(gfx, Color.argb(color), x, y, width, height);
    }

    public static void colorQuad(GuiGraphics gfx, float r, float g, float b, float a, double x, double y, double width, double height) {
        colorQuad(gfx, Color.of(r, g, b, a), x, y, width, height);
    }

    public static void colorQuad(GuiGraphics gfx, Color color, double x, double y, double width, double height) {
        //RenderSystem.disableTexture(); // TODO: still needed? probably not [the getPositionColorShader would ignore texture data anyway]
        //RenderSystem.disableDepthTest();
        //RenderSystem.colorMask(true, true, true, false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferbuilder.vertex(gfx.pose().last().pose(), (float)x, (float)(y + height), 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();

        bufferbuilder.vertex(gfx.pose().last().pose(), (float)x + (float)width, (float)(y + height), 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();

        bufferbuilder.vertex(gfx.pose().last().pose(), (float)x + (float)width, (float)y, 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();

        bufferbuilder.vertex(gfx.pose().last().pose(), (float)x, (float)y, 0.0f)
                .color(color.r, color.g, color.b, color.a)
                .endVertex();


        tesselator.end();
        RenderSystem.disableBlend();
        //RenderSystem.enableTexture(); // TODO still needed? probably not
        //RenderSystem.enableDepthTest();
    }

    public static final Vector3f XN = new Vector3f(-1, 0, 0);
    public static final Vector3f YN = new Vector3f(0, -1, 0);
    public static final Vector3f ZN = new Vector3f(0, 0, -1);
    public static final Vector3f XP = new Vector3f(1, 0, 0);
    public static final Vector3f YP = new Vector3f(0, 1, 0);
    public static final Vector3f ZP = new Vector3f(0, 0, 1);

    public static Quaternionf rotateAround(Vector3f axis, float degrees) {
        return new Quaternionf().rotateAxis((float)Math.toRadians(degrees), axis);
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }
}
