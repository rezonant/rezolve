package org.torchmc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class Label extends WidgetBase {
    public Label(Font font, Component initialContent, int x, int y, int width) {
        super(initialContent, x, y, width, 0);
        this.font = font;

        setContent(initialContent);
    }

    public static final int DEFAULT_COLOR = 4210752;

    public Label(Font font, Component initialContent, int x, int y) {
        this(font, initialContent, x, y, 99999);
    }

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

    public FormattedText getContent() {
        return content;
    }

    public void setContent(Component content) {
        this.content = content;
        this.narrationTitle = content;
        this.label = MultiLineLabel.create(font, content, width);
    }

    @Override
    protected void didResize() {
        setContent(content);
    }

    boolean visible = true;

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
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
}
