package com.rezolvemc.thunderbolt.tesseract.network;

import com.rezolvemc.common.network.RezolveScreenPacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.thunderbolt.tesseract.ResourceChannel;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("tesseract_channel_removed")
public class ChannelRemoved extends RezolveScreenPacket {
    public ChannelRemoved(ResourceChannel channel) {
        this.channel = new ChannelListing(channel);
    }
    public ChannelRemoved() { }

    public ChannelListing channel;

    @Override
    public void read(FriendlyByteBuf buf) {
        channel = ChannelListing.of(buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(channel.serializeNBT());
    }
}
