package com.astronautlabs.mc.rezolve.common.network;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraftforge.network.NetworkDirection;

/**
 * An interface implemented by classes that receive a specifically routed Rezolve packet.
 * For instance, Menus can implement this to receive Packets derived from RezolveMenuPacket.
 */
public interface RezolvePacketReceiver {
    default void receivePacket(RezolvePacket rezolvePacket, NetworkDirection direction) {
        if (direction == NetworkDirection.PLAY_TO_CLIENT )
            RezolveMod.LOGGER.warn("Packet {} is not handled by {} on the client", rezolvePacket.getClass().getCanonicalName(), this.getClass().getCanonicalName());
        else if (direction == NetworkDirection.PLAY_TO_SERVER)
            RezolveMod.LOGGER.warn("Packet {} is not handled by {} on the server", rezolvePacket.getClass().getCanonicalName(), this.getClass().getCanonicalName());
    }

    default void receivePacketOnServer(RezolvePacket rezolvePacket) { receivePacket(rezolvePacket, NetworkDirection.PLAY_TO_SERVER); };
    default void receivePacketOnClient(RezolvePacket rezolvePacket) { receivePacket(rezolvePacket, NetworkDirection.PLAY_TO_CLIENT); }
}
