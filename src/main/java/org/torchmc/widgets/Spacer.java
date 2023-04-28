package org.torchmc.widgets;

import net.minecraft.network.chat.Component;
import org.torchmc.WidgetBase;

public class Spacer extends WidgetBase {
    public Spacer() {
        super(Component.empty());
        setGrowScale(1);
    }
}
