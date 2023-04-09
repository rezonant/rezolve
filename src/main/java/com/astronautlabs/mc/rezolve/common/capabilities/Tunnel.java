package com.astronautlabs.mc.rezolve.common.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A capability for proxy blocks to use that pipe mods like Rezolve can query for in order to extend cable networks
 * across physically distant locations. An example of a proxy block would be Compact Machines and the corresponding
 * face blocks used on the inside of the machines.
 */
public interface Tunnel {
    /**
     * Level that the proxy endpoint resides in.
     * @return
     */
    ResourceKey<Level> getDestinationLevel();

    /**
     * Block position of the other side of the proxy.
     * @return
     */
    BlockPos getDestinationPosition();

    /**
     * The face on the other side
     * @return
     */
    Direction getDestinationFace();
}
