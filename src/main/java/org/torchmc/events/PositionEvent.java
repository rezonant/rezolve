package org.torchmc.events;

public class PositionEvent extends Event {
    public PositionEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int x;
    public final int y;
}
