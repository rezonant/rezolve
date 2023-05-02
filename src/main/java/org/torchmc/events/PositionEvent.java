package org.torchmc.events;

import org.torchmc.util.Size;

public class PositionEvent extends Event {
    public PositionEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int x;
    public final int y;
}
