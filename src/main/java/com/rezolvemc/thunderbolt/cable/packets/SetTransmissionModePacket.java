package com.rezolvemc.thunderbolt.cable.packets;

import com.rezolvemc.common.network.RezolveMenuPacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.thunderbolt.cable.TransmissionMode;
import com.rezolvemc.thunderbolt.cable.TransmissionType;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("set_transmission_mode")
public class SetTransmissionModePacket extends RezolveMenuPacket {
    public Direction face;
    public TransmissionType type;
    public TransmissionMode mode;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        face = Direction.byName(buf.readUtf());
        type = TransmissionType.byKey(buf.readUtf());
        mode = TransmissionMode.byKey(buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeUtf(face.getName());
        buf.writeUtf(type.getKey());
        buf.writeUtf(mode.getKey());
    }
}
