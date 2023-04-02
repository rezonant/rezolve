package com.astronautlabs.mc.rezolve.common.inventory;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@RegistryId("set_ghost_slot")
public class SetGhostSlotPacket extends RezolveMenuPacket {
    public int slotId;
    public ItemStack stack;

    public void setSlot(Slot slot) {
        this.slotId = slot.index;
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        slotId = buf.readInt();
        stack = buf.readItem();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeInt(slotId);
        buf.writeItemStack(stack, false);
    }
}
