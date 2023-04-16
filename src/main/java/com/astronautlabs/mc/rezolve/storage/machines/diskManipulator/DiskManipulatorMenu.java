package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.gui.WithScreen;
import com.astronautlabs.mc.rezolve.common.machines.MachineMenu;
import com.astronautlabs.mc.rezolve.common.machines.Sync;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.storage.view.StorageViewSession;
import com.astronautlabs.mc.rezolve.storage.view.packets.StorageViewPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

@WithScreen(DiskManipulatorScreen.class)
public class DiskManipulatorMenu extends MachineMenu<DiskManipulatorEntity> {
    public DiskManipulatorMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public DiskManipulatorMenu(int containerId, Inventory playerInv, DiskManipulatorEntity te) {
        super(RezolveRegistry.menuType(DiskManipulatorMenu.class), containerId, playerInv, te);

        addSlot(new Slot(container, 0, 5, 6));
        addPlayerSlots(47, 131);

        if (te != null) {
            session = new StorageViewSession(this, te, (ServerPlayer) playerInv.player);
        }
    }

    StorageViewSession session;

    @Override
    public void receivePacketOnServer(RezolvePacket rezolvePacket) {
        if (rezolvePacket instanceof StorageViewPacket storageViewPacket) {
            session.handlePacket(storageViewPacket);
        } else {
            super.receivePacketOnServer(rezolvePacket);
        }
    }
}
