package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets;

import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.MachineListing;
import com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.RemoteShellOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class RemoteShellStatePacket extends RezolvePacket {
    public boolean active;
    public BlockPos remoteShellPosition;
    public String remoteShellDimension;
    public MachineListing activeMachine;

    @Override
    public void read(FriendlyByteBuf buf) {
        active = buf.readBoolean();
        if (active) {
            remoteShellPosition = buf.readBlockPos();
            remoteShellDimension = buf.readUtf();
            activeMachine = MachineListing.of(buf.readNbt());
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
        }
    }

    @Override
    protected void receiveOnClient() {
        RemoteShellOverlay.INSTANCE.updateState(this);
    }
}
