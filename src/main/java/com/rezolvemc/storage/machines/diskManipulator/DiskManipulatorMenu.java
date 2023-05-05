package com.rezolvemc.storage.machines.diskManipulator;

import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.storage.DiskItem;
import com.rezolvemc.storage.view.StorageViewSession;
import com.rezolvemc.storage.view.packets.StorageViewPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DiskManipulatorMenu extends MachineMenu<DiskManipulatorEntity> {
    public DiskManipulatorMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public DiskManipulatorMenu(int containerId, Inventory playerInv, DiskManipulatorEntity te) {
        super(containerId, playerInv, te);

        addSlot(new Slot(container, 0, 5, 18));
        addPlayerSlots();

        if (te != null) {
            session = new StorageViewSession(this, te, (ServerPlayer) playerInv.player);
        }
    }

    StorageViewSession session;

    @Override
    protected void updateState() {
        super.updateState();
        session.checkForUpdates();
    }

    @Override
    public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
        if (rezolvePacket instanceof StorageViewPacket storageViewPacket) {
            session.handlePacket(storageViewPacket);
        } else {
            super.receivePacketOnServer(rezolvePacket, player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int sourceSlotId) {
        if (machine == null)
            return ItemStack.EMPTY;

        if (sourceSlotId == 0) {
            return super.quickMoveStack(pPlayer, sourceSlotId);
        } else {
            var item = getSlot(sourceSlotId).getItem();

            if (!machine.hasDisk() && item.getItem() instanceof DiskItem) {
                return super.quickMoveStack(pPlayer, sourceSlotId);
            } else {
                var result = session.giveItem(item, false);
                if (item.getCount() == result.getCount())
                    return ItemStack.EMPTY;

                getSlot(sourceSlotId).set(result);
                return result;
            }
        }
    }
}
