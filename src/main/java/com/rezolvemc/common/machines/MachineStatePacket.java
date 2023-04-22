package com.rezolvemc.common.machines;

import com.rezolvemc.common.blocks.RezolveBlockEntityPacket;
import com.rezolvemc.common.registry.RegistryId;
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
