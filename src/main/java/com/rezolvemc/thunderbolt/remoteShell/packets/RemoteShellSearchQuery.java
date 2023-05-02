package com.rezolvemc.thunderbolt.remoteShell.packets;

import com.rezolvemc.common.blocks.RezolveBlockEntityPacket;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("remote_shell_search_query")
public class RemoteShellSearchQuery extends RezolveBlockEntityPacket {
    public String query;
    public int offset;
    public int limit;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        query = buf.readUtf();
        offset = buf.readInt();
        limit = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeUtf(query);
        buf.writeInt(offset);
        buf.writeInt(limit);
    }
}
