package com.astronautlabs.mc.rezolve.common.network;

import net.minecraft.network.FriendlyByteBuf;

public class RezolvePacketServerBoundEnvelope extends RezolvePacketEnvelope {

    static RezolvePacketServerBoundEnvelope parse(FriendlyByteBuf buf) {
        var envelope = new RezolvePacketServerBoundEnvelope();
        envelope.read(buf);
        return envelope;
    }

    static RezolvePacketServerBoundEnvelope enclose(RezolvePacket packet) {
        var envelope = new RezolvePacketServerBoundEnvelope();
        envelope.packet = packet;
        return envelope;
    }
}
