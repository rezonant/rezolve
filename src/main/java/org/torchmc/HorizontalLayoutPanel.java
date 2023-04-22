package org.torchmc;

/**
 * Lays out child panels in a horizontal configuration.
 */
public class HorizontalLayoutPanel extends LayoutPanel {
    private int space;
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
        if (widget.getDesiredSize() == null)
            throw new IllegalArgumentException("Children of VerticalLayoutPanel must implement getDesiredSize()");

        return super.addChild(widget);
    }

    @Override
    public Size getDesiredSize() {
        var total = new Size(padding * 2, padding * 2);

        for (var child : children) {
            if (!child.isVisible())
                continue;

            var size = child.getDesiredSize();
            total.width += size.width + space;
            total.height = Math.max(total.height, size.height + padding * 2);
        }

        if (children.size() > 0)
            total.width -= space;

        return total;
    }

    @Override
    protected void updateLayout() {

        int availableSpace = width;
        int expansion = 0;

        for (var child : children) {
            if (!child.isVisible())
                continue;

            var size = child.getDesiredSize();
            availableSpace -= size.width;

            var grow = child.getGrowScale();
            if (grow != null) {
                expansion += grow.width;
            }
        }

        int x = padding;

        for (var child : children) {
            if (!child.isVisible())
                continue;

            var size = child.getDesiredSize();
            int width = size.width;
            Size grow = child.getGrowScale();

            if (grow != null && expansion > 0 && grow.width > 0)
                width += (grow.width / (double)expansion) * availableSpace;

            child.move(x, padding, width, height - padding * 2);
            x += size.width + space;
        }
    }
}
