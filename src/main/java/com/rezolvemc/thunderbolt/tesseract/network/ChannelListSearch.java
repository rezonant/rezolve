package com.rezolvemc.thunderbolt.tesseract.network;

import com.rezolvemc.common.blocks.RezolveBlockEntityPacket;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("tesseract_channel_list_search")
public class ChannelListSearch extends RezolveBlockEntityPacket {
    public String query;
    public int limit;
    public int offset;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        query = buf.readUtf();
        limit = buf.readInt();
        offset = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeUtf(query != null ? query : "");
        buf.writeInt(limit);
        buf.writeInt(offset);
    }
}
