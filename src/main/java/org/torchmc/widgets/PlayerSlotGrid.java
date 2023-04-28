package org.torchmc.widgets;

import com.rezolvemc.Rezolve;
import org.torchmc.layout.VerticalLayoutPanel;

public class PlayerSlotGrid extends VerticalLayoutPanel {
    public PlayerSlotGrid() {
        int firstSlot = firstPlayerSlotId();

        if (firstSlot < 0)
            throw new RuntimeException("PlayerSlotGrid can only be used with a menu that has declared player slots");

        addChild(new SlotGrid(Rezolve.tr("screens.rezolve.inventory"), 9), grid -> {
            grid.setLabelVisible(false);
            grid.setContents(firstSlot, 27);

            mainGrid = grid;
        });

        addChild(new SlotGrid(Rezolve.tr("screens.rezolve.hotbar"), 9), grid -> {
            grid.setLabelVisible(false);
            grid.setContents(firstSlot + 27, 9);

            hotbar = grid;
        });

        setSpace(5);
    }

    SlotGrid mainGrid;
    SlotGrid hotbar;

    private int firstPlayerSlotId() {
        for (int i = 0, max = screen.getMenu().slots.size(); i < max; ++i) {
            var slot = screen.getMenu().getSlot(i);

            if (slot.container == minecraft.player.getInventory()) {
                return i;
            }
        }

        return -1;
    }
}
