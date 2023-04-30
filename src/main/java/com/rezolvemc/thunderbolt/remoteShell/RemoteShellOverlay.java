package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.Rezolve;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellEntityReturnPacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStartRecordingPacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStatePacket;
import com.rezolvemc.thunderbolt.remoteShell.packets.RemoteShellStopRecordingPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.torchmc.TorchScreen;
import org.torchmc.Window;
import org.torchmc.layout.Axis;
import org.torchmc.layout.AxisAlignment;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.util.Color;
import org.torchmc.widgets.Button;
import org.torchmc.widgets.IconButton;
import org.torchmc.widgets.Label;
import org.torchmc.widgets.Meter;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Rezolve.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RemoteShellOverlay extends Window {
    public RemoteShellOverlay() {
        super(Component.empty());
        setup();
    }

    private boolean recording = false;
    private static RemoteShellStatePacket state;

    private void setup() {
        setClosable(false);
        setResizable(false);
        setMovable(false);
        setTitleBarVisible(false);

        setOnTick(() -> setVisible(state != null && state.active));

        setPanel(new HorizontalLayoutPanel(), status -> {
            status.setAlignment(AxisAlignment.CENTER);
            status.addChild(new Label(Rezolve.tr("block.rezolve.remote_shell")), label -> {
                label.setColor(Color.PURPLE);
                label.setRightPadding(10);
            });
            status.addChild(new Label(""), label -> {
                label.setExpansionFactor(1);
                label.setOnTick(() -> {
                    if (state != null && state.activeMachine != null) {
                        var itemStack = state.activeMachine.getItem();
                        var itemName = itemStack.getItem().getName(itemStack).plainCopy();

                        if (state.activeMachine.getName() != null) {
                            label.setContent(
                                    Component.empty()
                                            .append(Component.literal(state.activeMachine.getName()))
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

            status.addChild(new IconButton(), button -> {
                button.setBackgroundColor(Color.TRANSPARENT);
                button.setOnTick(() -> {
                    button.setIcon(Rezolve.icon(recording ? "stop" : "record"));
                    button.setTooltip(Rezolve.str(recording ? "stop" : "record"));
                });
                button.setHandler(() -> toggleRecording());
            });

            status.addChild(new Label(Rezolve.str("recording")), label -> {
                label.setOnTick(() -> label.setVisible(recording));
            });

            status.addChild(new Meter(Component.literal("Energy"), Component.literal("FE"), Rezolve.tex("gui/widgets/energy_meter.png")), meter -> {
                meter.setOrientation(Axis.X);
                meter.setWidthConstraint(AxisConstraint.atLeast(40));
                meter.setOnTick(() -> {
                    if (state == null)
                        return;

                    meter.setMax(state.remoteShellEnergyCapacity);
                    meter.setValue(state.remoteShellEnergy);
                });
            });
            status.addChild(new Button("Return"), button -> {
                button.setHandler(btn -> {
                    if (state == null)
                        return;

                    System.out.println("You tried to return to the remote shell!");
                    var returnMessage = new RemoteShellEntityReturnPacket();
                    returnMessage.dimension = state.remoteShellDimension;
                    returnMessage.blockPos = state.remoteShellPosition;
                    returnMessage.sendToServer();
                });
            });
        });

        int xPadding = 6;
        move(xPadding, 3, screen.width - xPadding * 2, 33);
    }

    private void toggleRecording() {
        if (recording)
            stopRecording();
        else
            startRecording();
    }

    private void startRecording() {
        recording = true;

        var packet = new RemoteShellStartRecordingPacket();
        packet.dimension = state.remoteShellDimension;
        packet.blockPos = state.remoteShellPosition;
        packet.sendToServer();
    }

    private void stopRecording() {
        recording = false;

        var packet = new RemoteShellStopRecordingPacket();
        packet.dimension = state.remoteShellDimension;
        packet.blockPos = state.remoteShellPosition;
        packet.sendToServer();
    }

    public static void updateState(RemoteShellStatePacket newState) {
        state = newState;
    }

    @SubscribeEvent
    public static void addOverlay(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?>) {
            var window = new RemoteShellOverlay();
            if (event.getScreen() instanceof TorchScreen<?> torchScreen)
                torchScreen.addWindow(window);
            else
                event.addListener(window);
        }
    }
}
