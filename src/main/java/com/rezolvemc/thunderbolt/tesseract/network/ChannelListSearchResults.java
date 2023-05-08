package com.rezolvemc.thunderbolt.tesseract.network;

import com.rezolvemc.common.network.RezolveScreenPacket;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

@RegistryId("tesseract_channel_list_search_results")
public class ChannelListSearchResults extends RezolveScreenPacket {
    public ChannelListSearchResults() {
    }

    public ChannelListSearchResults(List<ChannelListing> results) {
        this.results = results;
    }

    public List<ChannelListing> results = new ArrayList<>();


    @Override
    public void read(FriendlyByteBuf buf) {
        results.clear();
        var count = buf.readInt();
        for (int i = 0; i < count; ++i) {
            var listing = new ChannelListing();
            listing.deserializeNBT(buf.readNbt());
            results.add(listing);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(results.size());
        for (var listing : results) {
            buf.writeNbt(listing.serializeNBT());
        }
    }
}
