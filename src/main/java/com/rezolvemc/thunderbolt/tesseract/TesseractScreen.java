package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.ui.Window;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.*;
import org.torchmc.ui.util.Color;

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

    @Override
    protected void setup() {
        super.setup();

        addChild(new Window("Side Window"), window -> {
            window.setPanel(new VerticalLayoutPanel(), panel -> {
                panel.addChild(new Label("Cool!"), label -> {
                    label.setBackgroundColor(Color.PINK);
                });
            });

            window.move(100, 100, 100, 100);
        });

        leftShoulderButtons.addChild(new IconButton("Security?!", TEX_SECURITY_OPTIONS));
        leftShoulderButtons.addChild(new IconButton("Database", TEX_DATABASE_OPTIONS));
        leftShoulderButtons.addChild(new IconButton("Disk", TEX_DISK_OPTIONS));
        rightShoulderButtons.addChild(new IconButton("Storage", TEX_STORAGE_OPTIONS));
        rightShoulderButtons.addChild(new IconButton("Remoting", TEX_REMOTING_OPTIONS));

        setPanel(new VerticalLayoutPanel(), vert -> {
            vert.setSpace(4);
            vert.addChild(new Label("This is a test"));
            vert.addChild(new Button("Here's a button!"));
            vert.addChild(new EditBox("Edit box"));
            vert.addChild(new ListView("Channels"), listView -> {
                listView.setExpansionFactor(1);

                for (int i = 0, max = 25; i < max; ++i) {
                    listView.addItem("Test item");
                }
            });
            vert.addChild(new Label("This is another test"));
        });

    }
}
