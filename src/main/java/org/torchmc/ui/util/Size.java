package org.torchmc.ui.util;

public class Size {
    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size() {

    }

    public int width = 0;
    public int height = 0;

    public Size copy() {
        return new Size(width, height);
    }
}
