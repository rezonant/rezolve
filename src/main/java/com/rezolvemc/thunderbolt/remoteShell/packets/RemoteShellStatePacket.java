package com.rezolvemc.thunderbolt.remoteShell.packets;

import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.thunderbolt.remoteShell.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.RemoteShellOverlay;
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

    @Override
    public void read(FriendlyByteBuf buf) {
        active = buf.readBoolean();
        if (active) {
            remoteShellPosition = buf.readBlockPos();
            remoteShellDimension = buf.readUtf();
            activeMachine = MachineListing.of(buf.readNbt());
            remoteShellEnergy = buf.readInt();
            remoteShellEnergyCapacity = buf.readInt();
        } else {
            activeMachine = null;
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(active && activeMachine != null);
        if (active && activeMachine != null) {
            buf.writeBlockPos(remoteShellPosition);
            buf.writeUtf(remoteShellDimension);
            buf.writeNbt(activeMachine.serializeNBT());
            buf.writeInt(remoteShellEnergy);
            buf.writeInt(remoteShellEnergyCapacity);
        }
    }

    @Override
    protected void receiveOnClient() {
        RemoteShellOverlay.updateState(this);
    }
}
