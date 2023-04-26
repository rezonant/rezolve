package org.torchmc.layout;

import org.torchmc.util.Size;
import org.torchmc.WidgetBase;

/**
 * Lays out child panels in a vertical configuration.
 */
public class VerticalLayoutPanel extends LayoutPanel {
    int padding = 0;
    int space = 3;

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
        return super.addChild(widget);
    }

    private Size cachedDesiredSize;

    @Override
    protected void hierarchyDidChange() {
        super.hierarchyDidChange();
        cachedDesiredSize = null;
    }

    @Override
    public Size getDesiredSize() {
        if (cachedDesiredSize != null)
            return cachedDesiredSize;

        var total = new Size(padding * 2, padding * 2);

        for (var panel : children) {
            if (!panel.isVisible())
                continue;
            var size = panel.getDesiredSize();
            if (size == null)
                size = new Size(0, 0);
            total.width = Math.max(total.width, size.width + padding * 2);
            total.height += size.height + space;
        }

        if (children.size() > 0)
            total.height -= space;

        return cachedDesiredSize = total;
    }

    @Override
    protected void updateLayout() {
        int availableSpace = height - padding*2;
        int expansion = 0;

        for (var panel : children) {
            if (!panel.isVisible())
                continue;

            Size size = panel.getDesiredSize();
            if (size == null) {
                size = new Size(0, 0);
            }

            Size grow = panel.getGrowScale();
            if (grow != null) {
                expansion += grow.height;
            }

            availableSpace -= size.height + space;

        }

        if (children.size() > 0)
            availableSpace += space;

        int y = padding;

        for (var child : children) {
            if (!child.isVisible())
                continue;

            var size = child.getDesiredSize();
            Size grow = child.getGrowScale();

            if (size == null) {
                size = new Size(0, 0);
                if (grow == null)
                    grow = new Size(1, 1);
            }

            int height = size.height;

            if (grow != null && expansion > 0 && grow.height > 0)
                height += Math.max(0, (grow.height / (double)expansion) * availableSpace);

            child.move(padding, y + space, width - padding * 2, height);
            y += height + space;
        }
    }
}
