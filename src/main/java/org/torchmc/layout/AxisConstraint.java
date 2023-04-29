package org.torchmc.layout;

public class AxisConstraint {
    public static final AxisConstraint FREE = new AxisConstraint(0, 0, 0);

    private AxisConstraint(int min, int desired, int max) {
        this.min = min;
        this.desired = desired;
        this.max = max;
        this.fixed = this.min > 0 && this.max > 0 && this.min == this.max && this.max == desired;
    }

    public final int min;
    public final int desired;
    public final int max;
    public final boolean fixed;

    public AxisConstraint add(int i) {
        return new AxisConstraint(min > 0 ? min + i : 0, desired + i, max > 0 ? max + i : 0);
    }

    public static AxisConstraint union(AxisConstraint a, AxisConstraint b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return new AxisConstraint(Math.max(a.min, b.min), Math.max(a.desired, b.desired), a.max == 0 || b.max == 0 ? 0 : Math.min(a.max, b.max));
    }

    public static AxisConstraint fixed(int size) {
        if (size == 0)
            return FREE;

        return new AxisConstraint(size, size, size);
    }

    public static AxisConstraint atLeast(int size) {
        return atLeast(size, 0);
    }

    public static AxisConstraint atLeast(int size, int desired) {
        if (size == 0 && desired == 0)
            return FREE;
        return new AxisConstraint(size, desired, 0);
    }

    public static AxisConstraint atMost(int size) {
        return atMost(size, 0);
    }

    public static AxisConstraint atMost(int size, int desired) {
        if (size == 0 && desired == 0)
            return FREE;
        return new AxisConstraint(0, desired, size);
    }

    public static AxisConstraint between(int min, int desired, int max) {
        if (min == 0 && max == 0 && desired == 0)
            return FREE;

        return new AxisConstraint(min, desired, max);
    }

    public static AxisConstraint between(int min, int max) {
        return between(min, 0, max);
    }
}
