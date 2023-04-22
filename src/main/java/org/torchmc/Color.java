package org.torchmc;

import com.mojang.blaze3d.systems.RenderSystem;

public class Color {
    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static final Color WHITE = Color.of(1, 1, 1, 1);
    public static final Color BLACK = Color.of(0, 0, 0, 1);

    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public void applyToShader() {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public int argb() {
        return (int)(a * 255.0f) << 24 | (int)(r * 255.0f) << 16 | (int)(g * 255.0f) << 8 | (int)(b * 255.0f);
    }

    public int rgba() {
        return (int)(r / 255.0f) << 24 | (int)(g / 255.0f) << 16 | (int)(b / 255.0f) << 8 | (int)(a / 255.0f);
    }

    public int abgr() {
        return (int)(a / 255.0f) << 24 | (int)(b / 255.0f) << 16 | (int)(g / 255.0f) << 8 | (int)(r / 255.0f);
    }

    public int bgra() {
        return (int)(b / 255.0f) << 24 | (int)(g / 255.0f) << 16 | (int)(r / 255.0f) << 8 | (int)(a / 255.0f);
    }

    public Color withAlpha(float alpha) {
        return new Color(r, g, b, alpha);
    }

    public Color invert() {
        return new Color(1.0f - r, 1.0f - g, 1.0f - b, a);
    }

    public Color solidAlpha() {
        return new Color(r, g, b, 1.0f);
    }

    public static Color rgba(int color) {
        float red = ((color >> 24) & 0xFF) / 255.0f;
        float green = ((color >> 16) & 0xFF) / 255.0f;
        float blue = ((color >> 8) & 0xFF) / 255.0f;
        float alpha = (color & 0xFF) / 255.0f;

        return of(red, green, blue, alpha);
    }

    public static Color argb(int color) {
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        return of(red, green, blue, alpha);
    }

    public static Color of(double r, double g, double b, double a) {
        return new Color((float) r, (float) g, (float) b, (float) a);
    }

    public static Color of(float r, float g, float b, float a) {
        return new Color(r, g, b, a);
    }

    public Color multiplyAlpha(float alpha) {
        return new Color(r, g, b, a * alpha);
    }
}
