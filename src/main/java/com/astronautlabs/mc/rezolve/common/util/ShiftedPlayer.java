package com.astronautlabs.mc.rezolve.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Used by the Remote Shell functionality to retain isValid() checks when not using the Remote Shell.
 */
public class ShiftedPlayer extends Player {

    public ShiftedPlayer(Player wrappedPlayer, BlockPos fakePosition) {
        super(wrappedPlayer.getLevel(), fakePosition, 0, wrappedPlayer.getGameProfile(), wrappedPlayer.getProfilePublicKey());
        this.wrappedPlayer = wrappedPlayer;
    }

    Player wrappedPlayer;

    @Override
    public boolean isSpectator() {
        return this.wrappedPlayer.isSpectator();
    }

    @Override
    public boolean isCreative() {
        return this.wrappedPlayer.isCreative();
    }
}