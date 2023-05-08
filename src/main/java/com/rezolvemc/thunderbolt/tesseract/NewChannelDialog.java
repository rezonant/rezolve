package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.Rezolve;
import org.torchmc.ui.Dialog;
import org.torchmc.ui.layout.AxisAlignment;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.util.Size;
import org.torchmc.ui.widgets.Button;
import org.torchmc.ui.widgets.EditBox;
import org.torchmc.ui.widgets.Label;
import org.torchmc.util.Values;

public class NewChannelDialog extends Dialog {
    public NewChannelDialog() {
        super(Rezolve.str("new_channel"));

        setMinSize(new Size(124, 85));
    }

    private EditBox nameBox;

    @Override
    protected void setup() {
        super.setup();

        setPanel(new VerticalLayoutPanel(), root -> {
            root.addChild(new Label(Rezolve.str("name")));
            root.addChild(new EditBox(Rezolve.str("name")), editBox -> {
                nameBox = editBox;
            });
            root.addChild(new HorizontalLayoutPanel(), buttons -> {
                buttons.setJustification(AxisAlignment.END);
                buttons.addChild(new Button(Rezolve.str("cancel")), btn -> btn.setHandler(x -> cancel()));
                buttons.addChild(new Button(Rezolve.str("ok")), btn -> {
                    btn.setOnTick(() -> btn.setActive(isValid()));
                    btn.setHandler(x -> finish());
                });
            });
        });
    }

    public boolean isValid() {
        return !Values.isEmpty(nameBox.getValue());
    }

    public void finish() {
        if (!isValid())
            return;
        finish(new SuccessResult(nameBox.getValue()));
    }

    public class SuccessResult extends Result {
        public SuccessResult(String name) {
            this.name = name;
        }

        public final String name;
    }
}
