package org.torchmc.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AxisLayoutPanel extends LayoutPanel {
    public AxisLayoutPanel(Axis axis) {
        super();

        this.axis = axis;
    }

    private Axis axis;

    @Override
    protected void updateLayout() {

        // As a special case (important while bootstrapping UIs), there is no layouting that can reasonably be done if
        // our contents are expected to fit into a zero-length space on either the main or cross dimensions.
        if (width == 0 || height == 0) {
            return;
        }

        int[] plan = plan(getAxis(axis.opposite()));
        int pos = 0;
        int error = plan[plan.length - 1];

        for (int i = 0, max = children.size(); i < max; ++i) {
            var child = children.get(i);
            child.setAxis(axis.opposite(), getAxis(axis.opposite()));
            child.setAxis(axis, plan[i]);
            child.move(axis == Axis.X ? pos : 0, axis == Axis.Y ? pos : 0);

            pos += child.getAxis(axis);
        }
    }

    private record Entry(int index, int value) {}

    private int[] plan(int crossSize) {
        int[] plan = new int[children.size() + 1]; // +1 for the remainder size
        int[] maxSizes = new int[children.size()];
        int[] growthFactors = new int[children.size()];

        int available = getAxis(axis);
        int growthMax = 0;
        List<Entry> desired = new ArrayList<>();

        for (int i = 0, imax = children.size(); i < imax; ++i) {
            var child = children.get(i);
            var constraint = child.getDesiredSize(axis, crossSize);
            plan[i] = constraint.min;
            available = Math.max(0, available - plan[i]);
            growthFactors[i] = child.getGrowScale(axis);
            growthMax += growthFactors[i];
            maxSizes[i] = constraint.max;

            if (constraint.desired > 0 && constraint.desired > constraint.min)
                desired.add(new Entry(i, constraint.desired));
        }

        // Apply desired sizes to the plan

        desired.sort(Comparator.comparingInt(a -> a.value));
        for (var entry : desired) {
            int amount = Math.min(entry.value, available);
            plan[entry.index] += amount;
            available = Math.max(0, available - amount);
            if (available == 0)
                break;
        }

        // Distribute remaining available space by weights

        if (growthMax > 0 && available > 0) {
            for (int order = 0; available > 0; ++order) {
                order += 1;
                boolean progressed = false;
                int weight = order % growthMax;
                for (int i = 0, w = 0, imax = children.size(); i < imax; ++i) {
                    w += growthFactors[i];

                    if (weight < w && (plan[i] < maxSizes[i] || maxSizes[i] == 0) && growthFactors[i] > 0) {
                        plan[i] += 1;
                        available -= 1;
                        progressed = true;
                        break;
                    }
                }

                if (!progressed)
                    break;
            }
        }

        plan[plan.length - 1] = available;
        return plan;
    }

    @Override
    public AxisConstraint getDesiredSize(Axis axis, int crossSize) {
        if (axis == this.axis) {
            return AxisConstraint.between(getMinimumSize(), getMaximumSize());
        } else {
            var plan = plan(0);
            var constrainedCrossAxis = 0;
            for (int i = 0, max = plan.length - 1; i < max; ++i) {
                constrainedCrossAxis += children.get(i).getConstrainedAxis(axis, plan[i]);
            }

            AxisConstraint size = null;
            for (int i = 0, max = plan.length - 1; i < max; ++i) {
                size = AxisConstraint.union(size, children.get(i).getDesiredSize(axis, 0));
            }

            return size;
        }
    }

    @Override
    public AxisConstraint getDesiredHeight(int assumedWidth) {
        return getDesiredSize(Axis.Y, assumedWidth);
    }

    @Override
    public AxisConstraint getDesiredWidth(int assumedHeight) {
        return getDesiredSize(Axis.X, assumedHeight);
    }

    private int getMinimumSize() {
        int minSize = 0;
        for (var child : children) {
            minSize += child.getDesiredSize(axis, 0).min;
        }

        return minSize;
    }

    private int getMaximumSize() {
        int maxSize = 0;
        for (var child : children) {
            int size = child.getDesiredSize(axis, 0).max;
            if (size == 0)
                return 0;
            maxSize += size;
        }

        return maxSize;
    }
}
