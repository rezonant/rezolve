package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.torchmc.TorchWidget;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Color;
import org.torchmc.util.TorchUtil;

import java.util.List;

public class IconButton extends TorchWidget {
    public static final int SIZE = 18;

    public IconButton(Component text, ResourceLocation icon, int size) {
        super(Component.empty());

        this.text = text;
        this.icon = icon;
        this.size = size;
        this.width = size;
        this.height = size;

        setFocusable(true);
    }

    public IconButton() {
        this(Component.empty(), null, SIZE);
    }

    public IconButton(String text, ResourceLocation icon) {
        this(Component.literal(text), icon);
    }

    public IconButton(Component text, ResourceLocation icon) {
        this(text, icon, SIZE);
    }

    private Runnable handler;
    private Color activeTextColor = Color.argb(0xFFFFFFFF);
    private Color inactiveTextColor = Color.argb(0xFFA0A0A0);
    private Component text;
    private ResourceLocation icon;
    private float alpha = 1;
    private int size = SIZE;

    public void setIcon(ResourceLocation icon) {
        this.icon = icon;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public List<Component> getTooltip() {
        var tooltip = super.getTooltip();
        if (tooltip == null) {
            tooltip = List.of(text);
        }

        return tooltip;
    }

    public Runnable getHandler() {
        return handler;
    }

    public void setHandler(Runnable handler) {
        this.handler = handler;
    }

    public float getAlpha() {
        return alpha;
    }
    public void setAlpha(float alpha) {
        this.alpha = alpha;
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
    public int getConstrainedHeight(int width) {
        return size;
    }

    @Override
    public int getConstrainedWidth(int height) {
        return size;
    }

    @Override
    public AxisConstraint getDesiredHeight(int assumedWidth) {
        return AxisConstraint.fixed(size);
    }

    @Override
    public AxisConstraint getDesiredWidth(int assumedHeight) {
        return AxisConstraint.fixed(size);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        this.resize(size, size);
    }

    private Color backgroundColor = Color.BLACK;

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (isHoveredOrFocused()) {
            TorchUtil.colorQuad(pPoseStack, 0xFFFFFFFF, x, y, width, height);
        }

        TorchUtil.colorQuad(pPoseStack, backgroundColor, x + 1, y + 1, width - 2, height - 2);
        if (icon != null)
            TorchUtil.textureQuad(pPoseStack, icon, x + 1, y + 1, width - 2, height - 2, 0, 0, 1, 1);
    }

    boolean pressed = false;

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        pressed = true;
        if (handler != null)
            handler.run();
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
}
