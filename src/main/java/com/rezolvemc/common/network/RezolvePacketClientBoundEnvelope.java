package com.rezolvemc.common.network;

import net.minecraft.network.FriendlyByteBuf;

public class RezolvePacketClientBoundEnvelope extends RezolvePacketEnvelope {

    static RezolvePacketClientBoundEnvelope parse(FriendlyByteBuf buf) {
        var envelope = new RezolvePacketClientBoundEnvelope();
        envelope.read(buf);
        return envelope;
    }

    static RezolvePacketClientBoundEnvelope enclose(RezolvePacket packet) {
        var envelope = new RezolvePacketClientBoundEnvelope();
        envelope.packet = packet;
        return envelope;
    }
}
