package org.torchmc.layout;

import org.torchmc.WidgetBase;

public enum Axis {
    X, Y;

    public Axis opposite() {
        return this == X ? Y : X;
    }
}
