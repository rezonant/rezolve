package org.torchmc.ui;

import org.torchmc.ui.layout.Panel;

/**
 * A simple Panel type which supports free-form layout.
 */
public class CanvasPanel extends Panel {
    public <T extends TorchWidget> T addWidget(T widget) {
        return addChild(widget);
    }
}
