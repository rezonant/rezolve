package com.astronautlabs.mc.rezolve;

import net.minecraft.network.FriendlyByteBuf;

public interface IMessage {
    void read(FriendlyByteBuf buf);
    void write(FriendlyByteBuf buf);
}
