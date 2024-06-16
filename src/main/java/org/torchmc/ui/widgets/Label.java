package org.torchmc.ui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import org.torchmc.ui.TorchWidget;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.util.Color;
import org.torchmc.ui.util.TorchUtil;
import org.torchmc.util.Values;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Label extends TorchWidget {
    public Label(String text) {
        this(Component.literal(text));
    }


    public Label() {
        this(Component.empty());
    }

    public Label(Component initialContent) {
        super(initialContent);
        font = Minecraft.getInstance().font;
        setContent(initialContent);
    }

    public static final int DEFAULT_COLOR = 4210752;

    private Font font;
    private MultiLineLabel label;
    private Alignment alignment = Alignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private Component content;
    private Color color = Color.argb(DEFAULT_COLOR);
    private Color backgroundColor = Color.TRANSPARENT;

    public enum Alignment {
        LEFT,
        CENTERED
    }

    public enum VerticalAlignment {
        TOP,
        CENTER,
        BOTTOM
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setBackgroundColor(@Nonnull Color backgroundColor) {
        if (backgroundColor == null)
            throw new IllegalArgumentException("backgroundColor cannot be null");
        this.backgroundColor = backgroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public void setFont(Font font) {
        this.font = font;
        constructLabel();
    }

    public Font getFont() {
        return font;
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
        if (this.content == content || (this.content != null && content != null && Objects.equals(this.content.toString(), content.toString())))
            return;
        this.content = content;
        this.setMessage(content);
        constructLabel();
        notifyHierarchyChange();
    }

    private boolean wordWrapped = true;

    public boolean isWordWrapped() {
        return wordWrapped;
    }
    private void constructLabel() {
        this.label = MultiLineLabel.create(font, Values.coalesce(content, Component.empty()), width);
    }

    @Override
    protected void didResize() {
        constructLabel();
    }

    @Override
    public void renderContents(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        if (!isVisible())
            return;

        var yOffset = 0;

        if (verticalAlignment == VerticalAlignment.CENTER) {
            yOffset = Math.max(0, height / 2 - (label.getLineCount() * font.lineHeight) / 2);
        } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
            yOffset = Math.max(0, height - label.getLineCount() * font.lineHeight);
        }

        if (backgroundColor != null && backgroundColor.a > 0)
            TorchUtil.colorQuad(gfx, backgroundColor, getX(), getY(), width, height);

        if (label != null) {
            if (alignment == Alignment.CENTERED)
                label.renderCentered(gfx, width, getX(), getY() + yOffset, color.argb());
            else if (alignment == Alignment.LEFT)
                label.renderLeftAlignedNoShadow(gfx, getX(), getY() + yOffset, font.lineHeight, color.argb());
        }
    }

    @Override
    public AxisConstraint getWidthConstraint(int assumedHeight) {
        var custom = super.getWidthConstraint(assumedHeight);
        var min = 0;
        var max = 0;
        var desired = font.width(content);
        if (custom != null) {
            min = custom.min;
            max = custom.max;
        }

        if (max > 0)
            desired = Math.min(max, desired);

        if (min > 0)
            desired = Math.max(min, desired);

        return AxisConstraint.between(min, desired, max);
    }

    @Override
    public AxisConstraint getHeightConstraint(int assumedWidth) {
        var content = Values.coalesce(this.content, Component.empty());
        var label = MultiLineLabel.create(font, content, assumedWidth);

        if (assumedWidth > 0)
            return AxisConstraint.fixed(label.getLineCount()*font.lineHeight);

        return AxisConstraint.between(font.lineHeight, font.lineHeight, font.width(content));
    }
}
