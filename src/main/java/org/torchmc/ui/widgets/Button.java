package org.torchmc.ui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;

import java.util.function.Consumer;

/**
 * A button
 */
public class Button extends TorchWidget {
    /**
     * Minecraft buttons are always 20 (virtual) pixels tall.
     * You can't do anything about it, and you should ask yourself why you want to.
     * User interface guidelines exist for reasons.
     */
    public static final int HEIGHT = 20;

    public Button(Component text) {
        super(Component.empty());

        this.text = text;
        setHeightConstraint(AxisConstraint.fixed(20));
        setFocusable(true);
    }

    public Button() {
        this(Component.empty());
    }

    public Button(String text) {
        this(Component.literal(text));
    }

    private Consumer<Integer> handler;
    private Color activeTextColor = Color.argb(0xFFFFFFFF);
    private Color inactiveTextColor = Color.argb(0xFFA0A0A0);
    private Component text;
    private float alpha = 1;

    public float getAlpha() {
        return alpha;
    }
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Consumer<Integer> getHandler() {
        return handler;
    }

    public void setHandler(Consumer<Integer> handler) {
        this.handler = handler;
    }

    protected int getTextureY() {
        int i = 1;
        if (!isActive() || pressed || keyPressed)
            i = 0;
        else if (isHovered())
            i = 2;

        return 46 + i * 20;
    }

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public Color getInactiveTextColor() {
        return inactiveTextColor;
    }

    public void setInactiveTextColor(Color inactiveTextColor) {
        this.inactiveTextColor = inactiveTextColor;
    }

    public Color getActiveTextColor() {
        return activeTextColor;
    }

    public void setActiveTextColor(Color activeTextColor) {
        this.activeTextColor = activeTextColor;
    }

    @Override
    protected void renderContents(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {

        float focusBorder = 2;

        if (isFocused())
            TorchUtil.colorQuad(gfx, 0xFFFFFFFF, getX() - focusBorder, getY() - focusBorder, width + focusBorder*2, height + focusBorder*2);

        scissor(gfx, getX(), getY(), width, height, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            gfx.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            gfx.blitNineSliced(
                WIDGETS_LOCATION,
                this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                20, 4, 200, 20, 0,
                this.getTextureY()
            );

            gfx.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            // TODO: vanilla now has scrolling strings
            gfx.drawCenteredString(minecraft.font, text,
                    this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2,
                    activeTextColor.multiplyAlpha(this.alpha).argb()
            );
        });
    }

    boolean pressed = false;
    boolean keyPressed = false;

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_SPACE) {
            keyPressed = true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (keyPressed && isFocused() && handler != null) {
            handler.accept(0);
        }
        keyPressed = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        pressed = true;
        if (handler != null)
            handler.accept(pButton);
        return true;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        pressed = false;
        return true;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, text);
    }

    @Override
    public AxisConstraint getWidthConstraint(int assumedHeight) {
        if (super.getWidthConstraint(assumedHeight) != AxisConstraint.FREE)
            return super.getWidthConstraint(assumedHeight);

        return AxisConstraint.atLeast(font.width(text) + 12);
    }

    @Override
    public AxisConstraint getHeightConstraint(int assumedWidth) {
        return AxisConstraint.fixed(20);
    }
}
