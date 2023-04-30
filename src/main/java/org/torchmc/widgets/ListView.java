package org.torchmc.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.torchmc.TorchUI;
import org.torchmc.TorchWidget;
import org.torchmc.util.Color;
import org.torchmc.util.TorchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An extensible scrollable list view
 */
public class ListView extends TorchWidget {
    public ListView(Component narrationTitle) {
        super(narrationTitle);

        scrollbar = addChild(new ScrollBar() {
            @Override
            public void scrollPositionChanged(int position) {
                scrollPos = position;
            }
        });
        setFocusable(true);
    }

    public ListView(String narrationTitle) {
        this(Component.literal(narrationTitle));
    }

    private int scrollBarWidth = 3;
    private int focusedItem = 0;
    private ScrollBar scrollbar;
    private List<ListViewItem> items = new ArrayList<>();

    public void addItem(ListViewItem item) {
        items.add(item);
    }

    public void addItem(Component content) {
        addItem(new TextListViewItem(content));
    }

    public void addItem(String text) {
        addItem(new TextListViewItem(text));
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
    public void becameFocused() {
        ensureItemIsVisible(focusedItem);
        super.becameFocused();
    }

    @Override
    protected void didResize() {
        scrollbar.move(width - scrollBarWidth, 0, scrollBarWidth, height);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        setScrollPosition(Math.min(Math.max(0, (int)(scrollPos - pDelta * scrollSpeed)), Math.max(0, getTotalItemsHeight() - height)));
        return true;
    }

    void setScrollPosition(int pos) {
        scrollPos = pos;
        scrollbar.setScrollPosition(scrollPos);
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

    int getVisibleItemCount() {
        int count = 0;
        for (var item : items) {
            if (!item.isVisible())
                continue;
            count += 1;
        }

        return count;
    }

    public Rect2i getItemPosition(int itemIndex) {
        var result = new AtomicReference<Rect2i>();

        visitItems((item, x, y, index) -> {
            if (index == itemIndex) {
                result.set(new Rect2i(0, y, width, item.getHeight() + itemPadding * 2));
                return false;
            }

           return true;
        });

        return result.get();
    }

    public interface ListItemVisitor {
        boolean visit(ListViewItem item, int x, int y, int index);
    }

    public void visitItems(ListItemVisitor visitor) {
        visitItems(visitor, false);
    }

    public void visitItems(ListItemVisitor visitor, boolean includeInvisible) {
        int itemY = y - scrollPos;
        int index = 0;
        for (var item : items) {
            if (!includeInvisible && !item.isVisible())
                continue;

            if (!visitor.visit(item, 0, itemY, index))
                break;

            itemY += item.getHeight() + itemPadding*2;
            index += 1;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_DOWN) {
            focusedItem = Math.min(getVisibleItemCount() - 1, focusedItem + 1);
            ensureItemIsVisible(focusedItem);
        } else if (pKeyCode == GLFW.GLFW_KEY_UP) {
            focusedItem = Math.max(0, focusedItem - 1);
            ensureItemIsVisible(focusedItem);
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    void ensureItemIsVisible(int index) {
        var rect = getItemPosition(index);
        if (rect.getY() < y) {
            setScrollPosition(rect.getY() - y + scrollPos);
        } else if (rect.getY() + rect.getHeight() > y + height) {
            setScrollPosition(rect.getY() - y + scrollPos + rect.getHeight() - height);
        }
    }

    @Override
    public void renderContents(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

        scrollbar.setContentHeight(getTotalItemsHeight());

        boolean xInBounds = x < mouseX && mouseX < x + width;
        boolean mouseInView = isMouseOver(mouseX, mouseY);

        TorchUtil.insetBox(
                poseStack,
                TorchUI.builtInTex("gui/widgets/simple_frame.png"),
                x, y, width, height
        );

        scissor(poseStack, x, y, width, height, () -> {
            pushPose(poseStack, () -> {
                repose(() -> poseStack.translate(x + itemPadding, y - scrollPos + itemPadding, 0));

                visitItems((item, x, y, i) -> {
                    int itemY = y;

                    item.render(poseStack, width - itemPadding*2 - scrollBarWidth, mouseX, mouseY, partialTick);

                    TorchUtil.colorQuad(poseStack, 0x33000000, -itemPadding, item.getHeight() + itemPadding - 1, width - scrollBarWidth, 1);

                    if (isFocused() && focusedItem == i) {
                        TorchUtil.colorOutline(
                                poseStack, Color.WHITE, 2,
                                -itemPadding + 2, -itemPadding + 2,
                                width - scrollBarWidth - 4, item.getHeight()+itemPadding*2 - 4
                        );
                    }

                    if (mouseInView && xInBounds && itemY < mouseY && mouseY < itemY + itemPadding*2 + item.getHeight()) {
                        TorchUtil.colorQuad(poseStack, 0x33000000, -itemPadding, -itemPadding, width - scrollBarWidth, item.getHeight()+itemPadding*2);
                    }

                    repose(() -> poseStack.translate(0, item.getHeight() + itemPadding*2, 0));

                    return true;
                });
            });
        });
    }
}
