package org.torchmc.widgets;

import net.minecraft.network.chat.Component;
import org.torchmc.TorchWidget;

public class Spacer extends TorchWidget {
    public Spacer() {
        super(Component.empty());
        setExpansionFactor(1);
    }
}
