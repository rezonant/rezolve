package org.torchmc.events;

import org.torchmc.util.Size;

public class SizeEvent extends Event {
    public SizeEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int width;
    public final int height;
}
