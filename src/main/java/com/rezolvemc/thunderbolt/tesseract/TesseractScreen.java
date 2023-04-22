package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.layout.ContainerPanel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.widgets.Button;
import org.torchmc.widgets.Label;
import org.torchmc.widgets.ListView;
import org.torchmc.widgets.ListViewItem;

public class TesseractScreen extends MachineScreen<TesseractMenu> {
    public TesseractScreen(TesseractMenu menu, Inventory playerInventory, Component pTitle) {
        super(menu, playerInventory, pTitle, 256, 256);

        twoToneHeight = 0;
        enableInventoryLabel = false;
    }

    @Override
    protected void setup() {
        super.setup();

        addLeftShoulderButton(Component.literal("Test"), Rezolve.tex("blocks/block_security_server_front.png"), () -> {});
        addLeftShoulderButton(Component.literal("Test"), Rezolve.tex("blocks/block_database_server_front.png"), () -> {});
        addLeftShoulderButton(Component.literal("Test"), Rezolve.tex("blocks/block_disk_bay_front.png"), () -> {});

        addRightShoulderButton(Component.literal("Test"), Rezolve.tex("blocks/block_storage_shell_front.png"), () -> {});
        addRightShoulderButton(Component.literal("Test"), Rezolve.tex("blocks/block_remote_shell_front.png"), () -> {});

        var vert = setPanel(new VerticalLayoutPanel());
        vert.addChild(ContainerPanel.of(new Label(font, Component.literal("This is a test"), 0, 0, 0), 0, font.lineHeight));
        vert.setSpace(4);

        vert.addChild(new Button(0, 0, 0, Component.literal("Here's a button!"), () -> {}));
        vert.addChild(
            ContainerPanel.of(
                channelList = new ListView(
                    Component.literal("Channels"),
                    leftPos + 10, topPos + font.lineHeight * 2,
                    imageWidth - 20, imageHeight - font.lineHeight * 2 - 10
                ),
                imageWidth, 70
            ).withGrowScale(1)
        );
        vert.addChild(ContainerPanel.of(new Label(font, Component.literal("This is another test"), 0, 0, 0), 0, font.lineHeight));

        for (int i = 0, max = 25; i < max; ++i) {
            channelList.addItem(new ListViewItem() {
                @Override
                public void render(PoseStack poseStack, int width, int mouseX, int mouseY, float partialTicks) {
                    font.draw(poseStack, "Test item", 0, 0, 0xFF000000);
                }

                @Override
                public int getHeight() {
                    return font.lineHeight;
                }
            });
        }
    }

    ListView channelList;
}
