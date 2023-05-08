package org.torchmc.ui;

import com.rezolvemc.Rezolve;
import net.minecraft.network.chat.Component;
import org.torchmc.ui.layout.AxisAlignment;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.Button;
import org.torchmc.ui.widgets.Label;

public class ConfirmationDialog extends Dialog {
    public ConfirmationDialog(Component title, Component message) {
        super(title);

        messageLbl.setContent(message);
    }

    public ConfirmationDialog(String title, String message) {
        this(Component.literal(title), Component.literal(message));
    }

    private Label messageLbl;

    public static final Result CONFIRMED = new Result();

    @Override
    protected void setup() {
        super.setup();

        setPanel(new VerticalLayoutPanel(), root -> {
            root.addChild(new Label(), lbl -> {
                lbl.setWidthConstraint(AxisConstraint.atMost(200));
                messageLbl = lbl;
            });
            root.addChild(new HorizontalLayoutPanel(), buttons -> {
                buttons.setJustification(AxisAlignment.END);
                buttons.addChild(new Button(Rezolve.str("yes")), button -> button.setHandler(x -> finish(CONFIRMED)));
                buttons.addChild(new Button(Rezolve.str("no")), button -> button.setHandler(x -> finish(CANCELED)));
            });
        });
    }
}
