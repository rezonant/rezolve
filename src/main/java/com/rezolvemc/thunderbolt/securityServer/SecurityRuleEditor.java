package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.Rezolve;
import net.minecraft.network.chat.Component;
import org.torchmc.ui.Dialog;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.util.Size;
import org.torchmc.ui.widgets.Button;
import org.torchmc.ui.widgets.EditBox;
import org.torchmc.ui.widgets.Spacer;

import java.util.Objects;

public class SecurityRuleEditor extends Dialog {
    public SecurityRuleEditor(SecurityRule rule) {
        super(Rezolve.str("edit_security_rule"));
        this.rule = rule;

        if (Objects.equals(rule.name, "<machines>")) {
            setTitle(Rezolve.str("default_machine_policy"));
            playerModeBtn.setVisible(false);
        } else if (Objects.equals(rule.name, "<players>")) {
            setTitle(Rezolve.str("default_user_policy"));
            machineModeBtn.setVisible(false);
        } else {
        }

        nameField.setValue(rule.name);
        updateModeButtons();
    }

    public final SecurityRule rule;

    private Button machineModeBtn;
    private Button playerModeBtn;
    private EditBox nameField;

    @Override
    protected void setup() {
        super.setup();

        setMinSize(new Size(200, 100));
        setResizable(false);

        setPanel(new VerticalLayoutPanel(), root -> {
            root.addChild(new EditBox(Rezolve.str("name")), field -> {
                nameField = field;
            });
            root.addChild(new Button("Player Mode: X"), button -> {
                button.setHandler(x -> {
                    if (rule != null) {
                        int newMode = rule.getMode() + 1;
                        if (newMode > SecurityRule.MODE_OWNER)
                            newMode = SecurityRule.MODE_RESTRICTED;

                        rule.setMode(newMode);
                        this.updateModeButtons();
                    }
                });
                playerModeBtn = button;
            });
            root.addChild(new Button("Machine Mode: X"), button -> {
                button.setHandler(x -> {
                    if (rule != null) {
                        int newMode = rule.getMode() + 1;
                        if (newMode > SecurityRule.MODE_OWNER)
                            newMode = SecurityRule.MODE_OPEN;

                        rule.setMode(newMode);
                        this.updateModeButtons();
                    }
                });
                machineModeBtn = button;
            });

            root.addChild(new HorizontalLayoutPanel(), actionButtons -> {
                actionButtons.addChild(new Button(Rezolve.str("remove")));
                actionButtons.addChild(new Spacer());
                actionButtons.addChild(new Button((Rezolve.str("cancel"))), button -> button.setHandler(x -> cancel()));
                actionButtons.addChild(new Button((Rezolve.str("save"))), button -> button.setHandler(x -> finish(new Result())));
            });
        });
    }

    private void updateModeButtons() {
		if (Objects.equals(rule.getName(), "<machines>")) {
			this.machineModeBtn.setText(getMachinePolicyMode(rule.getMode()));
		} else {
			this.playerModeBtn.setText(getUserPolicyMode(rule.getMode()));
		}
    }

    public static Component getUserPolicyMode(int mode) {
        Component modeStr = Component.empty()
            .append(Component.translatable("screens.rezolve.unknown"))
            .append(" [")
            .append(mode + "")
            .append("]")
            ;

        switch (mode) {
            case SecurityRule.MODE_RESTRICTED:
                modeStr = Component.translatable("screens.rezolve.restricted");
                break;
            case SecurityRule.MODE_ALLOWED:
                modeStr = Component.translatable("screens.rezolve.allowed");
                break;
            case SecurityRule.MODE_OWNER:
                modeStr = Component.translatable("screens.rezolve.owner");
                break;
        }

        return modeStr;
    }

    public static Component getMachinePolicyMode(int mode) {
        Component modeStr = Component.empty()
            .append(Component.translatable("screens.rezolve.unknown"))
            .append(" [")
            .append(mode+"")
            .append("]");

        switch (mode) {
            case SecurityRule.MODE_NONE:
                modeStr = Component.translatable("screens.rezolve.none");
                break;
            case SecurityRule.MODE_OPEN:
                modeStr = Component.translatable("screens.rezolve.open");
                break;
            case SecurityRule.MODE_PROTECTED:
                modeStr = Component.translatable("screens.rezolve.protected");
                break;
            case SecurityRule.MODE_OWNER:
                modeStr = Component.translatable("screens.rezolve.owners_only");
                break;
        }

        return modeStr;
    }

}
