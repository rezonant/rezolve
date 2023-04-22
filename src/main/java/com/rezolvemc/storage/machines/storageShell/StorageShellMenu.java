package com.rezolvemc.storage.machines.storageShell;

import com.rezolvemc.common.registry.WithScreen;
import com.rezolvemc.common.machines.MachineMenu;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.storage.view.StorageViewSession;
import com.rezolvemc.storage.view.packets.StorageViewPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@WithScreen(StorageShellScreen.class)
public class StorageShellMenu extends MachineMenu<StorageShellEntity> {
    public StorageShellMenu(int pContainerId, Inventory playerInventory) {
        this(pContainerId, playerInventory, null);
    }

    public StorageShellMenu(int pContainerId, Inventory playerInventory, StorageShellEntity machine) {
        super(RezolveRegistry.menuType(StorageShellMenu.class), pContainerId, playerInventory, machine);

        addSlotGrid(0, 7, 131, 3, 3);
        addPlayerSlots(88, 131);

        if (machine != null) {
            session = new StorageViewSession(this, machine, (ServerPlayer) playerInventory.player);
        }
    }

    StorageViewSession session;

    @Override
    protected void updateState() {
        super.updateState();
        session.checkForUpdates();
    }


    @Override
    public void receivePacketOnServer(RezolvePacket rezolvePacket) {
        if (rezolvePacket instanceof StorageViewPacket storageViewPacket) {
            session.handlePacket(storageViewPacket);
        } else {
            super.receivePacketOnServer(rezolvePacket);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int sourceSlotId) {
        if (machine == null)
            return ItemStack.EMPTY;

        var item = getSlot(sourceSlotId).getItem();
        var result = session.giveItem(item, false);
        if (item.getCount() == result.getCount())
            return ItemStack.EMPTY;

        getSlot(sourceSlotId).set(result);
        return result;
    }
}
