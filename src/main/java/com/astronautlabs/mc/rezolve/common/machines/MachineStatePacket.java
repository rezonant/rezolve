package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.common.blocks.RezolveBlockEntityPacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("machine")
public class MachineStatePacket extends RezolveBlockEntityPacket {
    public float progress;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);

        progress = buf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeFloat(progress);
    }
}
