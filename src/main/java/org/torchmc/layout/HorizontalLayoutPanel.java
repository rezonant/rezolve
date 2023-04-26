package org.torchmc.layout;

import org.torchmc.util.Size;
import org.torchmc.WidgetBase;

/**
 * Lays out child panels in a horizontal configuration.
 */
public class HorizontalLayoutPanel extends LayoutPanel {
    private int space = 3;
    private int padding;

    public int getSpace() {
        return space;
    }

    public int getPadding() {
        return padding;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public void setPadding(int padding) {
        this.padding = padding;
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

        for (var child : children) {
            if (!child.isVisible())
                continue;

            var size = child.getDesiredSize();
            if (size == null)
                size = new Size(0, 0);

            total.width += size.width + space;
            total.height = Math.max(total.height, size.height + padding * 2);
        }

        if (children.size() > 0)
            total.width -= space;

        return cachedDesiredSize = total;
    }

    @Override
    protected void updateLayout() {
        int availableSpace = width;
        int expansion = 0;

        for (var child : children) {
            if (!child.isVisible())
                continue;


            Size size = child.getDesiredSize();
            if (size == null) {
                size = new Size(0, 0);
            }

            Size grow = child.getGrowScale();
            if (grow != null) {
                expansion += grow.width;
            }

            availableSpace -= size.width + space;

        }

        if (children.size() > 0)
            availableSpace += space;

        int x = padding;

        for (var child : children) {
            if (!child.isVisible())
                continue;

            Size size = child.getDesiredSize();
            if (size == null) {
                size = new Size(0, 0);
            }

            Size grow = child.getGrowScale();
            if (grow != null && expansion > 0 && grow.width > 0)
                size.width += (grow.width / (double)expansion) * availableSpace;

            child.move(x, padding, size.width, height - padding * 2);
            x += size.width + space;
        }
    }
}
