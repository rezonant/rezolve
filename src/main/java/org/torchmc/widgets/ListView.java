package org.torchmc.widgets;

import com.rezolvemc.Rezolve;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.torchmc.WidgetBase;
import org.torchmc.util.TorchUtil;

import java.util.ArrayList;
import java.util.List;

public class ListView extends WidgetBase {
    public ListView(Component narrationTitle, int x, int y, int width, int height) {
        super(narrationTitle, x, y, width, height);

        scrollbar = addChild(new ScrollBar(width - scrollBarWidth, 0, scrollBarWidth, height) {
            @Override
            public void scrollPositionChanged(int position) {
                scrollPos = position;
            }
        });
    }

    private int scrollBarWidth = 3;
    private ScrollBar scrollbar;
    private List<ListViewItem> items = new ArrayList<>();

    public void addItem(ListViewItem item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }

    public ListViewItem[] getItems() {
        return items.toArray(new ListViewItem[items.size()]);
    }

    private int scrollPos = 0;
    private int scrollSpeed = 9;
    private int itemPadding = 10;

    public int getItemPadding() {
        return itemPadding;
    }

    public void setItemPadding(int itemPadding) {
        this.itemPadding = itemPadding;
    }

    public void setScrollSpeed(int scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }

    public int getScrollSpeed() {
        return scrollSpeed;
    }

    public int getTotalItemsHeight() {
        int totalHeight = 0;
        for (var item : items) {
            totalHeight += item.getHeight() + itemPadding*2;
        }

        return totalHeight;
    }

    @Override
    protected void didResize() {
        scrollbar.move(width - scrollBarWidth, 0, scrollBarWidth, height);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        scrollPos = Math.min(Math.max(0, (int)(scrollPos - pDelta * scrollSpeed)), Math.max(0, getTotalItemsHeight() - height));
        scrollbar.setScrollPosition(scrollPos);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
        if (x < mouseX && mouseX < x + width - scrollBarWidth) {
            int itemY = y - scrollPos;

            for (var item : items) {
                if (itemY < mouseY && mouseY < itemY + itemPadding * 2 + item.getHeight()) {
                    item.mouseClicked(pButton);
                    return true;
                }
                itemY += item.getHeight() + itemPadding * 2;
            }
        }

        return super.mouseClicked(mouseX, mouseY, pButton);
    }

    @Override
    public void renderContents(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

        scrollbar.setContentHeight(getTotalItemsHeight());

        boolean xInBounds = x < mouseX && mouseX < x + width;
        boolean mouseInView = isMouseOver(mouseX, mouseY);

        TorchUtil.insetBox(poseStack, Rezolve.tex("gui/widgets/simple_frame.png"), x, y, width, height);
        scissor(poseStack, x, y, width, height, () -> {
            pushPose(poseStack, () -> {
                repose(poseStack, () -> poseStack.translate(x + itemPadding, y - scrollPos + itemPadding, 0));

                int itemY = y - scrollPos;

                for (var item : items) {
                    item.render(poseStack, width - itemPadding*2 - scrollBarWidth, mouseX, mouseY, partialTick);

                    TorchUtil.colorQuad(poseStack, 0x33000000, -itemPadding, item.getHeight() + itemPadding - 1, width - scrollBarWidth, 1);

                    if (mouseInView && xInBounds && itemY < mouseY && mouseY < itemY + itemPadding*2 + item.getHeight()) {
                        TorchUtil.colorQuad(poseStack, 0x33000000, -itemPadding, -itemPadding, width - scrollBarWidth, item.getHeight()+itemPadding*2);
                    }

                    repose(poseStack, () -> poseStack.translate(0, item.getHeight() + itemPadding*2, 0));
                    itemY += item.getHeight() + itemPadding*2;
                }
            });
        });
    }
}
