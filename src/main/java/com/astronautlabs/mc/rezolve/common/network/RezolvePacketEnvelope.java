package com.astronautlabs.mc.rezolve.common.network;

import net.minecraft.network.FriendlyByteBuf;

public class RezolvePacketEnvelope extends RezolvePacket {
    RezolvePacket packet;

    @Override
    public void read(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        packet = RezolvePacketEnvelope.parse(id, buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(packet.getRegistryKey());
        packet.write(buf);
    }
}
