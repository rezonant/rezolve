package org.torchmc.ui.layout;

/**
 * Represents a constraint along an axis. A constraint can specify minimum, desired, and maximum pixel sizes.
 * Zero represents an infinite size (aka a "free" constraint).
 */
public class AxisConstraint {
    /**
     * A special "unconstrained constraint" which places no restrictions on size. This is used as the default
     * constraint. Attempting to create a constraint with all zero sizes will always result in the FREE object.
     */
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

    /**
     * Create a new constraint by taking the current one and adding the given amount of pixels to each element.
     * @param i
     * @return
     */
    public AxisConstraint add(int i) {
        return new AxisConstraint(min > 0 ? min + i : 0, desired + i, max > 0 ? max + i : 0);
    }

    /**
     * Create a new constraint by combining the two given constraints. If either is null, the other is returned. If
     * both are null, null is returned. Otherwise, the largest minimum of the two is taken, the largest desired size of
     * the two is taken, and the smallest maximum size of the two is taken.
     * @param a
     * @param b
     * @return
     */
    public static AxisConstraint union(AxisConstraint a, AxisConstraint b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return new AxisConstraint(Math.max(a.min, b.min), Math.max(a.desired, b.desired), a.max == 0 || b.max == 0 ? 0 : Math.min(a.max, b.max));
    }

    /**
     * Create a fixed size constraint indicating that the only valid size is exactly the size passed.
     * @param size
     * @return
     */
    public static AxisConstraint fixed(int size) {
        if (size == 0)
            return FREE;

        return new AxisConstraint(size, size, size);
    }

    /**
     * Create a constraint with the given minimum size, an unbounded desired size and an unbounded maximum size.
     * @param size
     * @return
     */
    public static AxisConstraint atLeast(int size) {
        return atLeast(size, 0);
    }

    /**
     * Create a constraint with the given minimum and desired sizes, and an unbounded maximum size.
     * @param size
     * @param desired
     * @return
     */
    public static AxisConstraint atLeast(int size, int desired) {
        if (size == 0 && desired == 0)
            return FREE;
        return new AxisConstraint(size, desired, 0);
    }

    /**
     * Create a constraint with unbounded minimum and desired sizes and the given maximum size.
     * @param size
     * @return
     */
    public static AxisConstraint atMost(int size) {
        return atMost(size, 0);
    }

    /**
     * Create a constraint with unbounded minimum size and the given size for desired and maximum.
     * @param size
     * @param desired
     * @return
     */
    public static AxisConstraint atMost(int size, int desired) {
        if (size == 0 && desired == 0)
            return FREE;
        return new AxisConstraint(0, desired, size);
    }

    /**
     * Create a constraint with the given minimum, desired, and maximum sizes.
     * @param min
     * @param desired
     * @param max
     * @return
     */
    public static AxisConstraint between(int min, int desired, int max) {
        if (min == 0 && max == 0 && desired == 0)
            return FREE;

        return new AxisConstraint(min, desired, max);
    }

    /**
     * Create a constraint with the given minimum and maximum sizes. The desired size will be unbounded.
     * @param min
     * @param max
     * @return
     */
    public static AxisConstraint between(int min, int max) {
        return between(min, 0, max);
    }
}
