package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.torchmc.WidgetBase;
import org.torchmc.util.Size;

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
    private Component content;
    private int color = DEFAULT_COLOR;

    public enum Alignment {
        LEFT,
        CENTERED
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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
        setContent(content);
    }

    @Override
    public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!isVisible())
            return;

        if (alignment == Alignment.CENTERED)
            label.renderCentered(pPoseStack, width, x, y, color);
        else if (alignment == Alignment.LEFT)
            label.renderLeftAlignedNoShadow(pPoseStack, x, y, font.lineHeight, color);
    }

    @Override
    public Size getDesiredSize() {
        var custom = super.getDesiredSize();
        if (custom == null) {
            if (width == 0)
                return new Size(font.width(content), font.lineHeight);
            else
                return new Size(0, font.lineHeight * label.getLineCount());
        }

        return custom;
    }
}
