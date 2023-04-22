package com.rezolvemc.common.network;

import com.rezolvemc.Rezolve;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RezolvePacketEnvelope extends RezolvePacket {
    private static Logger LOGGER = LogManager.getLogger(Rezolve.ID);

    RezolvePacket packet;

    @Override
    public void read(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        packet = parse(id, buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        if (null == packet.getRegistryKey()) {
            throw new RuntimeException(
                    String.format(
                            "Packet %s is missing @RegistryId(), which implies it was not registered.",
                            getClass().getCanonicalName()
                    )
            );
        }

        buf.writeUtf(packet.getRegistryKey());
        packet.write(buf);
    }
}
