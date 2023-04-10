package com.astronautlabs.mc.rezolve.common.gui;

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
}
