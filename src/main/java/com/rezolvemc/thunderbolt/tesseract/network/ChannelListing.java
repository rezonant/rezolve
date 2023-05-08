package com.rezolvemc.thunderbolt.tesseract.network;

import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.thunderbolt.tesseract.ResourceChannel;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Objects;

public class ChannelListing implements INBTSerializable<CompoundTag> {
    public ChannelListing() {
    }

    public ChannelListing(ResourceChannel channel) {
        this.uuid = channel.getUuid();
        this.name = channel.getName();
    }

    public String uuid;
    public String name;

    public static ChannelListing of(CompoundTag tag) {
        var listing = new ChannelListing();
        listing.deserializeNBT(tag);
        return listing;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        tag.putString("uuid", uuid);
        tag.putString("name", name);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        uuid = nbt.getString("uuid");
        name = nbt.getString("name");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChannelListing listing) {
            return Objects.equals(uuid, listing.uuid);
        }

        return false;
    }
}
