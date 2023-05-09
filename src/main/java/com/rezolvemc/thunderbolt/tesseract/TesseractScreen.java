package com.rezolvemc.thunderbolt.tesseract;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.network.RezolveScreenPacket;
import com.rezolvemc.common.registry.ScreenFor;
import com.rezolvemc.thunderbolt.tesseract.network.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.torchmc.ui.ConfirmationDialog;
import org.torchmc.ui.Window;
import org.torchmc.ui.layout.AxisAlignment;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.*;
import org.torchmc.ui.util.Color;
import org.torchmc.util.Values;

import java.util.List;

@ScreenFor(TesseractMenu.class)
public class TesseractScreen extends MachineScreen<TesseractMenu> {
    public static final ResourceLocation TEX_SECURITY_OPTIONS = Rezolve.tex("blocks/block_security_server_front.png");
    public static final ResourceLocation TEX_DATABASE_OPTIONS = Rezolve.tex("blocks/block_database_server_front.png");
    public static final ResourceLocation TEX_DISK_OPTIONS = Rezolve.tex("blocks/block_disk_bay_front.png");
    public static final ResourceLocation TEX_STORAGE_OPTIONS = Rezolve.tex("blocks/block_storage_shell_front.png");
    public static final ResourceLocation TEX_REMOTING_OPTIONS = Rezolve.tex("blocks/block_remote_shell_front.png");

    public TesseractScreen(TesseractMenu menu, Inventory playerInventory, Component pTitle) {
        super(menu, playerInventory, pTitle, 256, 256);
    }

    private TesseractChannelPicker channelPicker;

    @Override
    protected void setup() {
        super.setup();

        channelPicker = new TesseractChannelPicker();
        leftShoulderButtons.addChild(new IconButton("Security?!", TEX_SECURITY_OPTIONS));
        leftShoulderButtons.addChild(new IconButton("Database", TEX_DATABASE_OPTIONS));
        leftShoulderButtons.addChild(new IconButton("Disk", TEX_DISK_OPTIONS));
        rightShoulderButtons.addChild(new IconButton("Storage", TEX_STORAGE_OPTIONS));
        rightShoulderButtons.addChild(new IconButton("Remoting", TEX_REMOTING_OPTIONS));

        setPanel(new VerticalLayoutPanel(), vert -> {
            vert.setSpace(4);
            vert.addChild(new Button(), button -> {
                button.setOnTick(() -> {
                    button.setText(
                        Component.empty()
                            .append(Rezolve.str("channel"))
                            .append(": ")
                            .append(menu.activeChannel == null ? "--" : menu.activeChannel.name)
                    );
                });

                button.setHandler(x -> channelPicker.present(result -> {
                    if (result instanceof ChannelPicker.ChannelChoiceResult choice) {
                        var packet = new SetActiveChannel();
                        packet.dimension = menu.dimension;
                        packet.blockPos = menu.blockPos;
                        packet.uuid = choice.channel.uuid;
                        packet.sendToServer();
                    }
                }));
            });
        });

        menu.listenForNextEvent(menu.READY, e -> channelPicker.refreshChannels());
    }

    List<ChannelListing> channels;

    @Override
    public void receivePacket(RezolveScreenPacket rezolveScreenPacket) {
        if (rezolveScreenPacket instanceof ChannelListSearchResults searchResults) {
            channelPicker.setChannels(searchResults.results);
        } else if (rezolveScreenPacket instanceof ChannelCreated || rezolveScreenPacket instanceof ChannelRemoved) {
            channelPicker.refreshChannels();
        } else {
            super.receivePacket(rezolveScreenPacket);
        }
    }

    public class TesseractChannelPicker extends ChannelPicker {
        @Override
        protected void removeChannel(ChannelListing channel) {
            var packet = new RemoveChannel();
            packet.dimension = menu.dimension;
            packet.blockPos = menu.blockPos;
            packet.uuid = channel.uuid;
            packet.sendToServer();
        }

        @Override
        protected void createChannel(String name) {
            var packet = new CreateChannel();
            packet.dimension = menu.dimension;
            packet.blockPos = menu.blockPos;
            packet.name = name;
            packet.sendToServer();
        }

        @Override
        protected void search(String query) {
            var packet = new ChannelListSearch();
            packet.dimension = menu.dimension;
            packet.blockPos = menu.blockPos;
            packet.query = query;
            packet.sendToServer();
        }
    }
}
