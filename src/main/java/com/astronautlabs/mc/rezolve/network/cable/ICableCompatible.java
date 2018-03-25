package com.astronautlabs.mc.rezolve.network.cable;

/**
 * Applied to a Block class to inform Rezolve's Cable system that this block
 * can conduct Omnicable signals. In order for a block to be truly Cable-compatible,
 * it must call CableNetwork.networkAt(world, pos).invalidate() if it is destroyed,
 * and CableNetwork.networkAt(world, pos).invalidateEndpoints() if one of its neighbors
 * changes.
 * This interface does not have any methods.
 */
public interface ICableCompatible {

}
