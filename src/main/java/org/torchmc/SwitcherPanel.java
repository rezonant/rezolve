package org.torchmc;

import com.mojang.blaze3d.vertex.PoseStack;

public class SwitcherPanel extends LayoutPanel {
    int activeIndex = 0;

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public Panel getActivePanel() {
        if (activeIndex < 0)
            return null;

        if (activeIndex >= children.size())
            return null;

        return (Panel)children.get(activeIndex);
    }

    @Override
    public void removePanel(Panel panel) {
        var activePanel = getActivePanel();
        super.removePanel(panel);

        if (activePanel != null) {
            if (activePanel == panel) {
                activePanel.didBecomeVisible();
                if (children.size() > 0) {
                    activeIndex = Math.min(activeIndex, children.size() - 1);
                    getActivePanel().didBecomeVisible();
                } else {
                    activeIndex = -1;
                }
            } else {
                activeIndex = children.indexOf(activePanel);
            }
        }
    }

    @Override
    protected void updateLayout() {
        for (var panel : children) {
            panel.move(0, 0, width, height);
        }
    }

    public void setActivePanel(Panel activePanel) {
        int index = this.children.indexOf(activePanel);
        if (index < 0)
            throw new RuntimeException("Given Panel is not a child of this PanelSwitcher");

        setActivePanel(index);
    }

    public void setActivePanel(int index) {
        if (index < 0 || index >= children.size())
            throw new IndexOutOfBoundsException(index);

        if (activeIndex == index)
            return;

        var previousIndex = activeIndex;
        var activePanel = getActivePanel();

        if (activePanel != null)
            activePanel.didBecomeInvisible();

        activeIndex = index;

        getActivePanel().didBecomeVisible();
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (getActivePanel() == null)
            return;

        getActivePanel().mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (getActivePanel() == null)
            return false;

        return getActivePanel().mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (getActivePanel() == null)
            return false;

        return getActivePanel().mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (getActivePanel() == null)
            return false;

        return getActivePanel().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (getActivePanel() == null)
            return false;

        return getActivePanel().mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        if (getActivePanel() == null)
            return false;

        return getActivePanel().isMouseOver(pMouseX, pMouseY);
    }

    @Override
    protected void renderChildren(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        var panel = getActivePanel();
        if (panel != null) {
            panel.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }
}
