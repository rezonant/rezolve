package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.machines.Sync;
import com.rezolvemc.thunderbolt.tesseract.network.ChannelListing;
import net.minecraft.world.entity.player.Inventory;

public class TesseractMenu extends MachineMenu<TesseractEntity> {
    public TesseractMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public TesseractMenu(int pContainerId, Inventory playerInventory, TesseractEntity machine) {
        super(pContainerId, playerInventory, machine);
    }

    @Sync public ChannelListing activeChannel;
    private ResourceChannel cachedActiveChannel;

    @Override
    protected void updateState() {
        super.updateState();

        if (cachedActiveChannel != this.machine.getChannel()) {
            cachedActiveChannel = this.machine.getChannel();
            activeChannel = new ChannelListing(this.machine.getChannel());
        }
    }
}
