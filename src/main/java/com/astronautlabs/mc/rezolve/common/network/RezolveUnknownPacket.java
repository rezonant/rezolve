package com.astronautlabs.mc.rezolve.common.network;

import net.minecraft.network.FriendlyByteBuf;

public class RezolveUnknownPacket extends RezolvePacket {
    RezolveUnknownPacket(String id) {
        this.id = id;
    }

    private String id;
    public String getId() {
        return this.id;
    }

    @Override
    public void read(FriendlyByteBuf buf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }
}
