package org.torchmc.ui.widgets;

import net.minecraft.network.chat.Component;
import org.torchmc.ui.TorchWidget;

/**
 * A no-op widget which has a default expansion factor of 1.
 */
public class Spacer extends TorchWidget {
    public Spacer() {
        super(Component.empty());
        setExpansionFactor(1);
    }
}
