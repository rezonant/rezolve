package org.torchmc.ui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.torchmc.ui.TorchWidget;
import org.torchmc.events.Event;
import org.torchmc.events.EventType;
import org.torchmc.ui.layout.AxisConstraint;

/**
 * A Torch widget that acts just like the vanilla EditBox with a few improvements.
 */
public class EditBox extends TorchWidget {
    public EditBox(Component narrationTitle) {
        super(narrationTitle);

        nativeWidget = new net.minecraft.client.gui.components.EditBox(font, 0, 0, 0, 0, Component.empty());
        nativeWidget.setResponder(value -> {
            valueDidChange(value);
            emitEvent(VALUE_CHANGED, new ValueEvent(value));
        });
        positionNativeWidget();
        setFocusable(true);

        setWidthConstraint(AxisConstraint.atLeast(18));
        setHeightConstraint(AxisConstraint.fixed(18));
    }

    public static final EventType<ValueEvent> VALUE_CHANGED = new EventType<>();
    public static final EventType<ValueEvent> ACTIVATED = new EventType<>();

    protected void valueDidChange(String value) {

    }

    private void positionNativeWidget() {
        var rect = getScreenRect();
        nativeWidget.x = rect.getX();
        nativeWidget.y = rect.getY();
        nativeWidget.setWidth(rect.getWidth());
        nativeWidget.setHeight(rect.getHeight());
    }

    public EditBox(String narrationTitle) {
        this(Component.literal(narrationTitle));
    }

    private net.minecraft.client.gui.components.EditBox nativeWidget;

    @Override
    protected void didResize() {
        super.didResize();

        positionNativeWidget();
    }

    public String getValue() {
        return nativeWidget.getValue();
    }

    public void setValue(String value) {
        nativeWidget.setValue(value);
    }

    @Override
    protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);

        positionNativeWidget();

        pushPose(pPoseStack, () -> {
            repose(() -> {
                var rect = getScreenRect();
                pPoseStack.translate(-rect.getX() + x, -rect.getY() + y, 0);
            });
            nativeWidget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        });
    }

    @Override
    public void becameUnfocused() {
        nativeWidget.setFocus(false);
    }

    @Override
    public void becameFocused() {
        nativeWidget.setFocus(true);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        takeFocus();

        if (pButton == 1) {
            nativeWidget.setValue("");
        }

        var rect = getScreenRect();
        return nativeWidget.mouseClicked(rect.getX() - x + pMouseX, rect.getY() - y + pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return nativeWidget.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        nativeWidget.mouseMoved(pMouseX - x, pMouseY - y);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return nativeWidget.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == InputConstants.KEY_RETURN) {
            wasActivated();
            emitEvent(ACTIVATED, new ValueEvent(getValue()));
            return true;
        }

        return nativeWidget.keyPressed(pKeyCode, pScanCode, pModifiers) || true;
    }

    protected void wasActivated() {

    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return nativeWidget.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return nativeWidget.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return super.isMouseOver(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return nativeWidget.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    private int maxLength = 32;
    public void setMaxLength(int i) {
        nativeWidget.setMaxLength(i);
        maxLength = i;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public class ValueEvent extends Event {
        public ValueEvent(String value) {
            this.value = value;
        }

        public final String value;
    }
}
