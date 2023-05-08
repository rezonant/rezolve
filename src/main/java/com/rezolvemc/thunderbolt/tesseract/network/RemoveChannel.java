package com.rezolvemc.thunderbolt.tesseract.network;

import com.rezolvemc.common.blocks.RezolveBlockEntityPacket;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("remove_tesseract_channel")
public class RemoveChannel extends RezolveBlockEntityPacket {
    public String uuid;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);

        uuid = buf.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);

        buf.writeUtf(uuid);
    }
}
