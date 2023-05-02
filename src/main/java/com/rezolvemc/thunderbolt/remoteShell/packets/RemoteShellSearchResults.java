package com.rezolvemc.thunderbolt.remoteShell.packets;

import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.thunderbolt.remoteShell.common.MachineListing;
import com.rezolvemc.thunderbolt.remoteShell.client.RemoteShellOverlay;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

@RegistryId("remote_shell_search_results")
public class RemoteShellSearchResults extends RezolvePacket {
    public List<MachineListing> machines = new ArrayList<>();
    public int offset;
    public int total;

    @Override
    public void read(FriendlyByteBuf buf) {
        var count = buf.readInt();
        machines = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            var listing = new MachineListing();
            listing.deserializeNBT(buf.readNbt());
            machines.add(listing);
        }
        offset = buf.readInt();
        total = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(machines.size());
        for (var machine : machines) {
            buf.writeNbt(machine.serializeNBT());
        }
        buf.writeInt(offset);
        buf.writeInt(total);
    }

    @Override
    protected void receiveOnClient() {
        RemoteShellOverlay.getSession().receiveResults(this);
    }
}
