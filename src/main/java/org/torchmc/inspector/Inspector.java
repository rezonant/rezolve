package org.torchmc.inspector;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.torchmc.TorchWidget;
import org.torchmc.Window;
import org.torchmc.events.Subscription;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.util.Color;
import org.torchmc.util.TorchUtil;
import org.torchmc.widgets.Label;
import org.torchmc.widgets.ListView;
import org.torchmc.widgets.ListViewItem;

public class Inspector extends Window {
    public Inspector(Window window) {
        super("Inspector");

        this.subject = window;

        move(10, 10, 256, 256);
        removeWhenDisposed(subject.addEventListener(HIERARCHY_CHANGED, e -> populateWidgets()));
        populateWidgets();
    }

    private Window subject;
    private ListView widgetList;

    @Override
    protected void setup() {
        super.setup();

        setPanel(new HorizontalLayoutPanel(), root -> {
            root.addChild(new VerticalLayoutPanel(), vert -> {
                vert.setExpansionFactor(1);
                vert.addChild(new ListView("Widgets"), view -> {
                    view.setItemPadding(4);
                    view.setExpansionFactor(1);

                    view.addEventListener(ListView.ITEM_HOVERED, e -> {
                       var item = (WidgetListItem)e.item;
                       highlightWidget(item.widget);
                    });

                    widgetList = view;
                });
            });
        });
    }

    TorchWidget highlightedWidget = null;
    Subscription highlightedWidgetSubscription = null;

    private void unhighlightWidget() {
        if (highlightedWidgetSubscription != null) {
            highlightedWidgetSubscription.unsubscribe();
        }

        if (highlightedWidget != null) {
            highlightedWidget = null;
        }
    }

    private Color highlightColor = new Color(0, 1, 0, 0.5);
    private Color outlineColor = new Color(0, 1, 0, 1);

    private void drawHighlight(TorchWidget widget, RenderEvent event) {
        TorchUtil.colorQuad(event.poseStack, highlightColor, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
        TorchUtil.colorOutline(event.poseStack, outlineColor, 2, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());

        pushPose(event.poseStack, () -> {
            repose(() -> {
                event.poseStack.translate(widget.getX(), widget.getY(), 0);
                event.poseStack.scale(0.5f, 0.5f, 1);
            });

            var text = String.format("%dx%d @ %d,%d", widget.getWidth(), widget.getHeight(), widget.getX(), widget.getY());

            TorchUtil.colorQuad(event.poseStack, Color.WHITE, - 2, - font.lineHeight - 2, font.width(text) + 4, font.lineHeight + 4);
            font.draw(event.poseStack, text, 0, - font.lineHeight, Color.BLACK.argb());
        });
    }

    private void highlightWidget(TorchWidget widget) {
        unhighlightWidget();
        highlightedWidget = widget;
        highlightedWidgetSubscription = widget.addEventListener(AFTER_RENDER, e -> drawHighlight(widget, e));
    }

    private int depthOf(TorchWidget widget) {
        int depth = 0;
        widget = widget.getParent();
        while (widget != null) {
            depth += 1;
            widget = widget.getParent();
        }

        return depth;
    }

    private void populateWidgets() {
        widgetList.clearItems();
        subject.visitAll(widget -> widgetList.addItem(new WidgetListItem(widget)));
    }

    public class WidgetListItem implements ListViewItem {
        public WidgetListItem(TorchWidget widget) {
            this.widget = widget;
            this.depth = depthOf(widget);
        }

        TorchWidget widget;
        int depth;

        @Override
        public void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks) {
            int indent = depth * 8;
            font.draw(poseStack, widget.getClass().getSimpleName(), indent, 0, Color.BLACK.argb());
            font.draw(poseStack, widget.getClass().getPackageName(), indent, font.lineHeight, Color.GRAY.argb());
        }

        @Override
        public int getHeight() {
            return font.lineHeight * 2;
        }
    }
}
