package org.torchmc.widgets;

import net.minecraft.network.chat.Component;
import org.torchmc.TorchWidget;

/**
 * A no-op widget which has a default expansion factor of 1.
 */
public class Spacer extends TorchWidget {
    public Spacer() {
        super(Component.empty());
        setExpansionFactor(1);
    }
}
