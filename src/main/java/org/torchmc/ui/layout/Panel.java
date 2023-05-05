package org.torchmc.ui.layout;

import net.minecraft.network.chat.Component;
import org.torchmc.ui.TorchWidget;

/**
 * Represents a non-visual widget whose primary purpose is to contain other widgets.
 */
public class Panel extends TorchWidget {
    public Panel() {
        super(Component.empty());
    }

}
