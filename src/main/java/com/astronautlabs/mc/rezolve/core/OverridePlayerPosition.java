package com.astronautlabs.mc.rezolve.core;

import com.astronautlabs.mc.rezolve.common.ShiftedPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OverridePlayerPosition {

	public static void setPlayerOverridePosition(UUID playerID, BlockPos pos) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.put(playerID.toString(), pos);
		}
	}

	public static void clearPlayerOverridePosition(UUID playerID) {
		synchronized (playerOverridePositions) {
			playerOverridePositions.remove(playerID.toString());
		}
	}


	public static Map<String, BlockPos> playerOverridePositions = new HashMap<String, BlockPos>();

	/**
	 * Determine if the player is allowed to interact with the given UI container.
	 * This overrides the EntityPlayer.onUpdate() check for container.canInteractWith(player).
	 *
	 * @param container
	 * @param player
	 * @return
	 */
	public static boolean canInteractWith(Object containerObj, Object playerObj) {

		Container container = (Container)containerObj;
		EntityPlayer player = (EntityPlayer)playerObj;

		// Security check


		if (container.canInteractWith(player)) {
			return true;
		}

		// Container is rejecting player, override if available

		synchronized (playerOverridePositions) {
			if (!playerOverridePositions.containsKey(player.getUniqueID().toString())) {
				return false;
			}

			BlockPos overriddenPosition = playerOverridePositions.get(player.getUniqueID().toString());
			boolean result = container.canInteractWith(new ShiftedPlayer(player, overriddenPosition));
			return result;
		}
	}

}
