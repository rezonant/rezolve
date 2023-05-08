package com.rezolvemc.thunderbolt.tesseract.network;

import com.rezolvemc.common.blocks.RezolveBlockEntityPacket;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("create_tesseract_channel")
public class CreateChannel extends RezolveBlockEntityPacket {
    public String name;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);

        name = buf.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);

        buf.writeUtf(name);
    }
}
