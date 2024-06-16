package org.torchmc.ui.widgets;

import com.rezolvemc.Rezolve;
import net.minecraft.client.gui.GuiGraphics;
import org.torchmc.ui.TorchUI;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.util.TorchUtil;

/**
 * A specialized item slot grid widget which automatically renders the player's inventory slots. Only useful when the
 * associated AbstractContainerMenu contains slots that connect to the player's inventory (player.getInventory())
 */
public class PlayerSlotGrid extends VerticalLayoutPanel {
    public PlayerSlotGrid() {
        int firstSlot = firstPlayerSlotId();

        if (firstSlot < 0)
            throw new RuntimeException("PlayerSlotGrid can only be used with a menu that has declared player slots");

        setTopPadding(8);

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

    @Override
    protected void renderBackground(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        TorchUtil.insetBox(gfx, TorchUI.builtInTex("gui/widgets/twotone_background.png"), getX() - 6, getY() - 6, width + 12, height + 2);
        super.renderBackground(gfx, pMouseX, pMouseY, pPartialTick);
    }
}
