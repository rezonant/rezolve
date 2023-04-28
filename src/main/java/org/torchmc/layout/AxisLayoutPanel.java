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

        int[] plan = plan(getAxis(axis) - space * (countVisibleChildren() - 1), getAxis(axis.opposite()));
        int pos = 0;
        int crossSize = getAxis(axis.opposite());

        for (int i = 0, max = children.size(); i < max; ++i) {
            var child = children.get(i);
            child.setAxis(axis.opposite(), crossSize);
            child.setAxis(axis, plan[i]);

            child.move((axis == Axis.X ? pos : 0) + child.getLeftPadding(), (axis == Axis.Y ? pos : 0) + child.getTopPadding());

            pos += child.getAxis(axis) + space;
        }
    }

    private record Entry(int index, int value) {}

    private int[] plan(int size, int crossSize) {
        int[] plan = new int[children.size() + 1]; // +1 for the remainder size
        int[] maxSizes = new int[children.size()];
        int[] growthFactors = new int[children.size()];

        int available = size;
        int growthMax = 0;
        List<Entry> desired = new ArrayList<>();

        for (int i = 0, imax = children.size(); i < imax; ++i) {
            var child = children.get(i);

            if (!child.isVisible()) {
                continue;
            }

            var constraint = child.getDesiredSize(axis, crossSize).add(child.getPadding(axis));

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
            plan[entry.index] = Math.max(plan[entry.index], amount);
            available = Math.max(0, available - amount);
            if (available == 0)
                break;
        }

        // Distribute remaining available space by weights

        if (growthMax > 0 && available > 0) {
            int[] initialSizes = new int[plan.length - 1];
            int proportionMax = available;
            for (int i = 0, max = initialSizes.length; i < max; ++i) {
                initialSizes[i] = plan[i];
                proportionMax += plan[i];
            }

            for (int order = 0; available > 0; ++order) {
                boolean progressed = false;
                int weight = order % growthMax;
                for (int i = 0, w = 0, imax = children.size(); i < imax; ++i) {
                    w += growthFactors[i];

                    if (plan[i] >= maxSizes[i] && maxSizes[i] != 0)
                        continue;

                    if (plan[i] > growthFactors[i] / (double)growthMax * proportionMax)
                        continue;


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
            var plan = plan(0, 0);

            AxisConstraint size = null;
            for (int i = 0, max = plan.length - 1; i < max; ++i) {
                size = AxisConstraint.union(size, children.get(i).getDesiredSize(axis, 0).add(children.get(i).getPadding(axis)));
            }

            if (size == null)
                size = AxisConstraint.FREE;

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

    private int space = 3;

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    private int getMinimumSize() {
        int minSize = 0;
        for (var child : children) {
            minSize += child.getDesiredSize(axis, 0).min;
        }

        return minSize + space * (countVisibleChildren() - 1);
    }

    private int countVisibleChildren() {
        int i = 0;
        for (var child : children) {
            if (child.isVisible())
                i += 1;
        }

        return i;
    }

    private int getMaximumSize() {
        int maxSize = 0;
        for (var child : children) {
            int size = child.getDesiredSize(axis, 0).max;
            if (size == 0)
                return 0;
            maxSize += size;
        }

        return maxSize + space * (countVisibleChildren() - 1);
    }
}
