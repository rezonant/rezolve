package org.torchmc.layout;

import net.minecraft.network.chat.Component;
import org.torchmc.TorchWidget;

/**
 * Represents a non-visual widget whose primary purpose is to contain other widgets.
 */
public class Panel extends TorchWidget {
    public Panel() {
        super(Component.empty());
    }

}
