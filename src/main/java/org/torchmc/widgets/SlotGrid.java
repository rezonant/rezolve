package org.torchmc.widgets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.torchmc.TorchWidget;
import org.torchmc.layout.AxisConstraint;

import java.util.ArrayList;
import java.util.List;

public class SlotGrid extends TorchWidget {
    public SlotGrid(Component label, int breadth) {
        super(label);

        this.label = label;
        this.breadth = breadth;
    }

    private Component label;
    private int breadth;
    private int cachedSlotCount;
    private Label labelWidget;
    private boolean labelVisible = true;

    public Component getLabel() {
        return label;
    }

    public void setLabel(Component label) {
        this.label = label;
    }

    public void setContents(Slot firstSlot, int count) {
        setContents(firstSlot.index, count);
    }

    public void setContents(int firstIndex, int count) {
        List<Slot> slots = new ArrayList<>();
        for (int i = firstIndex, max = firstIndex + count; i < max; ++i) {
            slots.add(screen.getMenu().getSlot(i));
        }

        setContents(slots);
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
        hierarchyDidChange();
    }

    public void setContents(List<Slot> slots) {
        children.clear();

        addChild(labelWidget = new Label());
        labelWidget.setContent(label);
        //labelWidget.setAlignment(Label.Alignment.CENTERED);

        int i = 0;
        cachedSlotCount = slots.size();
        for (var slot : slots) {
            addChild(new SlotWidget(
                    Component.empty()
                            .append(label)
                            .append(" ")
                            .append(Component.translatable("screens.resolve.slot"))
                            .append(" ")
                            .append((i + 1)+""),
                    slot
            ));
            i += 1;
        }
    }

    public SlotWidget[] getSlots() {
        List<SlotWidget> slots = new ArrayList<>();
        for (var child : children) {
            if (child instanceof SlotWidget slot)
                slots.add(slot);
        }

        return slots.toArray(new SlotWidget[slots.size()]);
    }

    private int labelMargin = 5;
    @Override
    public AxisConstraint getWidthConstraint(int assumedHeight) {
        return AxisConstraint.atLeast(SlotWidget.SIZE * breadth);
    }

    @Override
    public AxisConstraint getHeightConstraint(int assumedWidth) {
        int rowCount = (int)Math.ceil(cachedSlotCount / (float)breadth);
        return AxisConstraint.atLeast(SlotWidget.SIZE * rowCount + (labelVisible ? (font.lineHeight + labelMargin*2) : 0));
    }


    @Override
    protected void didResize() {
        super.didResize();

        var slots = getSlots();

        if (slots.length != cachedSlotCount)
            return;

        int rowCount = (int)Math.ceil(cachedSlotCount / (float)breadth);

        int gridWidth = SlotWidget.SIZE * breadth;
        int gridHeight = SlotWidget.SIZE * rowCount + (labelVisible ? font.lineHeight + labelMargin*2 : 0);
        int gridX = width / 2 - gridWidth / 2;
        int gridY = height / 2 - gridHeight / 2;

        labelWidget.setVisible(labelVisible);
        if (labelVisible) {
            labelWidget.move(gridX + gridWidth / 2 - font.width(label) / 2, gridY + labelMargin, font.width(label), font.lineHeight);
            gridY += font.lineHeight + labelMargin*2;
        }

        for (var row = 0; row < rowCount; ++row) {
            for (var col = 0; col < breadth; ++col) {
                slots[row * breadth + col].move(gridX + col * SlotWidget.SIZE, gridY + row * SlotWidget.SIZE);
            }
        }
    }
}
