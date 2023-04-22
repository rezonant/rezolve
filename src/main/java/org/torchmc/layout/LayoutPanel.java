package org.torchmc.layout;

import org.torchmc.WidgetBase;

public abstract class LayoutPanel extends Panel {

    @Override
    public <T extends WidgetBase> T addChild(T widget) {
        super.addChild(widget);
        updateLayout();
        return widget;
    }

    public void removePanel(Panel panel) {
        children.remove(panel);
    }

    @Override
    protected void didResize() {
        updateLayout();
    }

    protected abstract void updateLayout();
}
