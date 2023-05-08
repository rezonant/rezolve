package com.rezolvemc.thunderbolt.tesseract;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.Rezolve;
import com.rezolvemc.thunderbolt.tesseract.network.ChannelListing;
import net.minecraft.client.Minecraft;
import org.torchmc.ui.ConfirmationDialog;
import org.torchmc.ui.Dialog;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.*;

import java.util.List;

public class ChannelPicker extends Dialog {
    public ChannelPicker() {
        super(Rezolve.str("choose_channel"));
    }

    private EditBox searchBox;
    private ListView channelList;
    private ChannelListing selectedChannel;
    private List<ChannelListing> channels;

    protected void removeChannel(ChannelListing channel) {

    }

    protected void createChannel(String name) {

    }

    protected void search(String query) {

    }

    public void refreshChannels() {
        search(searchBox.getValue());
    }

    public void setChannels(List<ChannelListing> channels) {
        this.channels = channels;

        channelList.clearItems();

        for (var channel : channels) {
            channelList.addItem(new ChannelListViewItem(channel));
        }
    }

    @Override
    protected void setup() {
        super.setup();

        resize(241, 217);

        setPanel(new VerticalLayoutPanel(), vert -> {
            vert.setSpace(4);
            vert.addChild(new HorizontalLayoutPanel(), searchRow -> {
                searchRow.addChild(new Label(Rezolve.str("search")));
                searchRow.addChild(new EditBox(Rezolve.str("search")), editBox -> {
                    editBox.setExpansionFactor(1);
                    searchBox = editBox;
                });
            });

            vert.addChild(new ListView(Rezolve.str("channels")), listView -> {
                listView.setExpansionFactor(1);

                listView.addEventListener(ListView.ITEM_SELECTED, e -> selectChannel(((ChannelListViewItem)e.item).channel));

                channelList = listView;
            });

            vert.addChild(new HorizontalLayoutPanel(), align -> {
                align.addChild(new Button(Rezolve.str("remove")), button -> {
                    button.setOnTick(() -> button.setActive(selectedChannel != null));
                    button.setHandler(btn -> {
                        var listing = selectedChannel;

                        new ConfirmationDialog(Rezolve.str("are_you_sure"), Rezolve.str("are_you_sure_you_want_to_remove_this_channel"))
                                .present(result -> {
                                    if (result == ConfirmationDialog.CONFIRMED) {
                                        removeChannel(listing);
                                    }
                                })
                        ;
                    });
                });
                align.addChild(new Button(Rezolve.str("new")), button -> {
                    button.setHandler(btn -> new NewChannelDialog().present(r -> {
                        if (r instanceof NewChannelDialog.SuccessResult result) {
                            createChannel(result.name);
                        }
                    }));
                });
                align.addChild(new Spacer());
                align.addChild(new Button(Rezolve.str("cancel")), button -> {
                    button.setHandler(btn -> cancel());
                });
                align.addChild(new Button(Rezolve.str("choose")), button -> {
                    button.setOnTick(() -> button.setActive(selectedChannel != null));
                    button.setHandler(btn -> {
                        finish(new ChannelChoiceResult(selectedChannel));
                    });
                });
            });
        });
    }

    private void selectChannel(ChannelListing channel) {
        selectedChannel = channel;
    }

    public static class ChannelChoiceResult extends Result {
        public ChannelChoiceResult(ChannelListing channel) {
            this.channel = channel;
        }

        public final ChannelListing channel;
    }

    public static class ChannelListViewItem implements ListViewItem {
        public ChannelListViewItem(ChannelListing channel) {
            this.channel = channel;
        }

        private ChannelListing channel;

        @Override
        public void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks) {
            Minecraft.getInstance().font.draw(poseStack, channel.name, 0, 0, 0xFF000000);
        }

        @Override
        public int getHeight() {
            return Minecraft.getInstance().font.lineHeight;
        }
    }

}
