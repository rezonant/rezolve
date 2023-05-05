package org.torchmc.ui.layout;

/**
 * Base class for all layout panels
 */
public abstract class LayoutPanel extends Panel {
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
