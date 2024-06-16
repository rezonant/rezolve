package com.rezolvemc.thunderbolt.remoteShell.client;

import com.rezolvemc.Rezolve;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.torchmc.ui.Window;
import org.torchmc.ui.layout.Axis;
import org.torchmc.ui.layout.AxisAlignment;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.widgets.*;
import org.torchmc.ui.util.Color;

public class RemoteShellOverlay extends Window {
    public RemoteShellOverlay() {
        super(Component.empty());
    }

    private MachineListWindow machineList;

    public MachineListWindow getMachineList() {
        return machineList;
    }

    @Override
    protected void setup() {
        super.setup();

        setClosable(false);
        setResizable(false);
        setMovable(false);
        setTitleBarVisible(false);

        machineList = new MachineListWindow();
        machineList.setVisible(false);

        setOnTick(() -> setVisible(getSession() != null && getSession().active));

        setPanel(new HorizontalLayoutPanel(), status -> {
            status.setAlignment(AxisAlignment.CENTER);
            status.addChild(new Label(Rezolve.tr("block.rezolve.remote_shell")), label -> {
                label.setColor(Color.PURPLE);
                label.setRightPadding(10);
            });
            status.addChild(new Label(""), label -> {
                label.setExpansionFactor(1);
                label.setOnTick(() -> {
                    if (getSession() != null && getSession().activeMachine != null) {
                        var itemStack = getSession().activeMachine.getItem();
                        var itemName = itemStack.getItem().getName(itemStack).plainCopy();

                        if (getSession().activeMachine.getName() != null) {
                            label.setContent(
                                    Component.empty()
                                            .append(Component.literal(getSession().activeMachine.getName()))
                                            .append(
                                                    Component.empty()
                                                            .withStyle(ChatFormatting.GRAY)
                                                            .append(" (")
                                                            .append(itemName)
                                                            .append(")")
                                            )
                            );
                        } else {
                            label.setContent(itemName);
                        }
                    }
                });
            });

            status.addChild(new VirtualSlotWidget(Rezolve.str("recorded_pattern")), slot -> {
                slot.setOnTick(() -> {
                    if (slot.getItem() != getSession().recordedPattern) {
                        slot.setVisible(getSession().recordedPattern != null);
                        slot.setItem(getSession().recordedPattern);
                    }
                });
                slot.setItem(new ItemStack(Items.BUCKET));
                slot.setHandler(() -> {
                    if (getSession() == null)
                        return;

                    // In an integrated server context, we need to set the currently carried item without having the
                    // following mouse release event treated as an item drop.
                    minecraft.tell(() -> RemoteShellOverlay.getSession().takePattern(slot.getItem()));
                });
            });

            status.addChild(new IconButton(), button -> {
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setOnTick(() -> {
                    button.setIcon(Rezolve.icon(getSession() != null && getSession().recording ? "stop" : "record"));
                    button.setTooltip(Tooltip.create(Rezolve.str(getSession() != null && getSession().recording ? "stop" : "record")));
                });
                button.setHandler(() -> getSession().toggleRecording());
            });

            status.addChild(new Label(Rezolve.str("recording")), label -> {
                label.setOnTick(() -> label.setVisible(getSession() != null && getSession().recording));
            });

            status.addChild(new Meter(Component.literal("Energy"), Component.literal("FE"), Rezolve.tex("gui/widgets/energy_meter.png")), meter -> {
                meter.setOrientation(Axis.X);
                meter.setWidthConstraint(AxisConstraint.atLeast(40));
                meter.setOnTick(() -> {
                    if (getSession() == null)
                        return;

                    meter.setMax(getSession().remoteShellEnergyCapacity);
                    meter.setValue(getSession().remoteShellEnergy);
                });
            });
            status.addChild(new Button(Rezolve.str("switch")), button -> {
                button.setOnTick(() -> {
                    button.setVisible(getSession().activeMachine != null);
                    if (getSession().activeMachine == null)
                        machineList.setVisible(false);
                });
                button.setHandler(btn -> {
                    machineList.setVisible(true);
                });
            });
            status.addChild(new Button(Rezolve.str("return")), button -> {
                button.setOnTick(() -> {
                    button.setVisible(getSession().activeMachine != null);
                });
                button.setHandler(btn -> getSession().returnToShell());
            });
        });

        int xPadding = 6;
        move(xPadding, 3, screen.width - xPadding * 2, 33);

        machineList.move(getX(), getY() + height + 5, 255, 212);
    }

    public static RemoteAccessClientSession getSession() {
        return RemoteAccessClientSession.INSTANCE;
    }
}
