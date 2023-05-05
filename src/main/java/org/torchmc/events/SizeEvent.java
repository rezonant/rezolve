package org.torchmc.events;

public class SizeEvent extends Event {
    public SizeEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int width;
    public final int height;
}
