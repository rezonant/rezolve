package org.torchmc;

import org.torchmc.layout.Panel;

/**
 * A simple Panel type which supports free-form layout.
 */
public class CanvasPanel extends Panel {
    public <T extends WidgetBase> T addWidget(T widget) {
        return addChild(widget);
    }
}
