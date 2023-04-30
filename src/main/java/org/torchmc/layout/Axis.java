package org.torchmc.layout;

/**
 * Represents a 2D axis (X or Y)
 */
public enum Axis {
    X, Y;

    /**
     * Obtain the opposite axis, for X this is Y, for Y this is X.
     * @return
     */
    public Axis opposite() {
        return this == X ? Y : X;
    }
}
