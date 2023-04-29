package com.rezolvemc.common.network;

import com.rezolvemc.Rezolve;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;

/**
 * An interface implemented by classes that receive a specifically routed Rezolve packet.
 * For instance, Menus can implement this to receive Packets derived from RezolveMenuPacket.
 */
public interface RezolvePacketReceiver {
    default void receivePacket(RezolvePacket rezolvePacket, NetworkDirection direction) {
        if (direction == NetworkDirection.PLAY_TO_CLIENT )
            Rezolve.LOGGER.warn("Packet {} is not handled by {} on the client", rezolvePacket.getClass().getCanonicalName(), this.getClass().getCanonicalName());
        else if (direction == NetworkDirection.PLAY_TO_SERVER)
            Rezolve.LOGGER.warn("Packet {} is not handled by {} on the server", rezolvePacket.getClass().getCanonicalName(), this.getClass().getCanonicalName());
    }

    default void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) { receivePacket(rezolvePacket, NetworkDirection.PLAY_TO_SERVER); };
    default void receivePacketOnClient(RezolvePacket rezolvePacket) { receivePacket(rezolvePacket, NetworkDirection.PLAY_TO_CLIENT); }
}
