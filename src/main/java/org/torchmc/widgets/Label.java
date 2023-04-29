package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.torchmc.WidgetBase;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.util.Color;
import org.torchmc.util.Size;
import org.torchmc.util.TorchUtil;

import javax.annotation.Nonnull;

public class Label extends WidgetBase {
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
        this.content = content;
        this.narrationTitle = content;
        constructLabel();
        hierarchyDidChange();
    }

    private void constructLabel() {
        this.label = MultiLineLabel.create(font, content, width);
    }

    @Override
    protected void didResize() {
        constructLabel();
    }

    @Override
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!isVisible())
            return;

        var yOffset = 0;

        if (verticalAlignment == VerticalAlignment.CENTER) {
            yOffset = Math.max(0, height / 2 - (label.getLineCount() * font.lineHeight) / 2);
        } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
            yOffset = Math.max(0, height - label.getLineCount() * font.lineHeight);
        }

        if (backgroundColor != null && backgroundColor.a > 0)
            TorchUtil.colorQuad(pPoseStack, backgroundColor, x, y, width, height);

        if (label != null) {
            if (alignment == Alignment.CENTERED)
                label.renderCentered(pPoseStack, width, x, y + yOffset, color.argb());
            else if (alignment == Alignment.LEFT)
                label.renderLeftAlignedNoShadow(pPoseStack, x, y + yOffset, font.lineHeight, color.argb());
        }
    }

    @Override
    public AxisConstraint getDesiredWidth(int assumedHeight) {
        return AxisConstraint.between(0, font.width(content), 0);
    }

    @Override
    public AxisConstraint getDesiredHeight(int assumedWidth) {
        var label = MultiLineLabel.create(font, content, assumedWidth);

        if (assumedWidth > 0)
            return AxisConstraint.fixed(label.getLineCount()*font.lineHeight);

        return AxisConstraint.between(font.lineHeight, font.lineHeight, font.width(content));
    }
}
