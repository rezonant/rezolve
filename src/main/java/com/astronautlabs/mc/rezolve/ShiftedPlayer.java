package com.astronautlabs.mc.rezolve;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class ShiftedPlayer extends EntityPlayer {

	public ShiftedPlayer(EntityPlayer wrappedPlayer, BlockPos fakePosition) {
		super(wrappedPlayer.getEntityWorld(), wrappedPlayer.getGameProfile());
		this.wrappedPlayer = wrappedPlayer;
		this.posX = fakePosition.getX();
		this.posY = fakePosition.getY();
		this.posZ = fakePosition.getZ();
	}
	
	EntityPlayer wrappedPlayer;
	
	@Override
	public boolean isSpectator() {
		return this.wrappedPlayer.isSpectator();
	}

	@Override
	public boolean isCreative() {
		return this.wrappedPlayer.isCreative();
	}
}
