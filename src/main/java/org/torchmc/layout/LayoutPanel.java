package org.torchmc.layout;

import org.torchmc.TorchWidget;

/**
 * Base class for all layout panels
 */
public abstract class LayoutPanel extends Panel {

    @Override
    public <T extends TorchWidget> T addChild(T widget) {
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

    @Override
    protected void hierarchyDidChange() {
        updateLayout();
        super.hierarchyDidChange();
    }
}
