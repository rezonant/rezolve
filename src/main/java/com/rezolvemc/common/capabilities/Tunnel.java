package com.rezolvemc.common.capabilities;

import com.rezolvemc.common.LevelBlockFace;
import java.util.List;

/**
 * A capability for proxy blocks to use that pipe mods like Rezolve can query for in order to extend cable networks
 * across physically distant locations. An example of a proxy block would be Compact Machines and the corresponding
 * face blocks used on the inside of the machines.
 */
public interface Tunnel {
    List<LevelBlockFace> getProxyDestinations();
}
