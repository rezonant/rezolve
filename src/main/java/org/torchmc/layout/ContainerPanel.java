package org.torchmc.layout;

import org.torchmc.util.Size;
import org.torchmc.WidgetBase;

/**
 * Provides a fixed-size Panel which can contain a single Widget.
 */
public class ContainerPanel<T extends WidgetBase> extends LayoutPanel {
    private ContainerPanel(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static <T extends WidgetBase> ContainerPanel<T> of(T widget, int width, int height) {
        ContainerPanel<T> panel = new ContainerPanel(width, height);
        panel.addChild(widget);
        return panel;
    }

    public T getWidget() {
        return (T)children.get(0);
    }

    @Override
    public Size getDesiredSize() {
        var growScale = getGrowScale();
        var growScaleX = growScale != null ? growScale.width : 0;
        var growScaleY = growScale != null ? growScale.height : 0;
        return new Size(growScaleX > 0 ? 0 : width, growScaleY > 0 ? 0 : height);
    }

    @Override
    protected void updateLayout() {
        getWidget().move(0, 0, width, height);
    }

    public ContainerPanel withGrowScale(int scale) {
        setGrowScale(scale);
        return this;
    }

    public ContainerPanel withGrowScale(Size scale) {
        setGrowScale(scale);
        return this;
    }
}
