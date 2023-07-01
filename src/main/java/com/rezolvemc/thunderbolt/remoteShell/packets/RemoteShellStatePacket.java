package com.rezolvemc.thunderbolt.remoteShell.packets;

import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.client.RemoteShellOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

@RegistryId("remote_shell_state")
public class RemoteShellStatePacket extends RezolvePacket {
    public boolean active;
    public BlockPos remoteShellPosition;
    public String remoteShellDimension;
    public MachineListing activeMachine;
    public int remoteShellEnergy;
    public int remoteShellEnergyCapacity;
    public ItemStack recordedPattern;
    public boolean recording = false;
    public boolean hasDatabase = false;

    @Override
    public void read(FriendlyByteBuf buf) {
        hasDatabase = buf.readBoolean();
        active = buf.readBoolean();
        if (active) {
            remoteShellPosition = buf.readBlockPos();
            remoteShellDimension = buf.readUtf();

            if (buf.readBoolean())
                activeMachine = MachineListing.of(buf.readNbt());

            remoteShellEnergy = buf.readInt();
            remoteShellEnergyCapacity = buf.readInt();
            recording = buf.readBoolean();
            if (buf.readBoolean())
                recordedPattern = buf.readItem();
            else
                recordedPattern = null;
        } else {
            activeMachine = null;
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(hasDatabase);
        buf.writeBoolean(active);
        if (active) {
            buf.writeBlockPos(remoteShellPosition);
            buf.writeUtf(remoteShellDimension);

            buf.writeBoolean(activeMachine != null);
            if (activeMachine != null)
                buf.writeNbt(activeMachine.serializeNBT());
            buf.writeInt(remoteShellEnergy);
            buf.writeInt(remoteShellEnergyCapacity);
            buf.writeBoolean(recording);
            buf.writeBoolean(recordedPattern != null);
            if (recordedPattern != null)
                buf.writeItem(recordedPattern);
        }
    }

    @Override
    protected void receiveOnClient() {
        RemoteShellOverlay.getSession().updateState(this);
    }
}
