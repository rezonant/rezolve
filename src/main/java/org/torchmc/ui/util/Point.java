package org.torchmc.ui.util;

public class Point {
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public final double x;
    public final double y;

    public static Point of(double pMouseX, double pMouseY) {
        return new Point(pMouseX, pMouseY);
    }

    public Point add(int x, int y) {
        return new Point(this.x + x, this.y + y);
    }

    public Point add(Point p) {
        return new Point(x + p.x, y + p.y);
    }
}
