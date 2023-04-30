package org.torchmc.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.torchmc.TorchWidget;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Color;
import org.torchmc.util.TorchUtil;

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

    protected int getYImage(boolean pIsHovered) {
        if (!isActive() || pressed || keyPressed)
            return 0;

        return isHovered() ? 2 : 1;
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
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

        float focusBorder = 2;

        if (isFocused())
            TorchUtil.colorQuad(pPoseStack, 0xFFFFFFFF, x - focusBorder, y - focusBorder, width + focusBorder*2, height + focusBorder*2);

        scissor(pPoseStack, x, y, width, height, () -> {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHoveredOrFocused());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            //RenderSystem.enableDepthTest();

            this.blit(pPoseStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.blit(
                    pPoseStack,
                    this.x + this.width / 2, this.y,
                    200 - this.width / 2,
                    46 + i * 20, this.width / 2, this.height
            );

            drawCenteredString(pPoseStack, font, text,
                    this.x + this.width / 2, this.y + (this.height - 8) / 2,
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
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, text);
    }

    @Override
    public AxisConstraint getDesiredWidth(int assumedHeight) {
        if (super.getDesiredWidth(assumedHeight) != AxisConstraint.FREE)
            return super.getDesiredWidth(assumedHeight);

        return AxisConstraint.atLeast(font.width(text) + 12);
    }

    @Override
    public AxisConstraint getDesiredHeight(int assumedWidth) {
        return AxisConstraint.fixed(20);
    }
}
