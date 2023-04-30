package org.torchmc.layout;

public enum Axis {
    X, Y;

    public Axis opposite() {
        return this == X ? Y : X;
    }
}
