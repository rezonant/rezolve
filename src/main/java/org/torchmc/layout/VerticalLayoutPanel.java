package org.torchmc.layout;

import org.torchmc.util.Size;
import org.torchmc.WidgetBase;

/**
 * Lays out child panels in a vertical configuration.
 */
public class VerticalLayoutPanel extends LayoutPanel {
    int padding = 0;
    int space = 0;

    public int getPadding() {
        return padding;
    }

    public int getSpace() {
        return space;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    @Override
    public <T extends WidgetBase> T addChild(T widget) {
        if (widget.getDesiredSize() == null)
            throw new IllegalArgumentException("Children of VerticalLayoutPanel must implement getDesiredSize()");

        return super.addChild(widget);
    }

    @Override
    public Size getDesiredSize() {
        var total = new Size(padding * 2, padding * 2);

        for (var panel : children) {
            if (!panel.isVisible())
                continue;
            var size = panel.getDesiredSize();
            total.width = Math.max(total.width, size.width + padding * 2);
            total.height += size.height + space;
        }

        if (children.size() > 0)
            total.height -= space;

        return total;
    }

    @Override
    protected void updateLayout() {
        int availableSpace = height - padding*2;
        int expansion = 0;

        for (var panel : children) {
            if (!panel.isVisible())
                continue;

            var size = panel.getDesiredSize();
            availableSpace -= size.height + space;

            var grow = panel.getGrowScale();
            if (grow != null) {
                expansion += grow.height;
            }
        }

        if (children.size() > 0)
            availableSpace += space;

        int y = padding;

        for (var child : children) {
            if (!child.isVisible())
                continue;

            var size = child.getDesiredSize();
            int height = size.height;
            Size grow = child.getGrowScale();

            if (grow != null && expansion > 0 && grow.height > 0)
                height += Math.max(0, (grow.height / (double)expansion) * availableSpace);

            child.move(padding, y + space, width - padding * 2, height);
            y += height + space;
        }
    }
}
