//package com.astronautlabs.mc.rezolve;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.entity.player.Player;
//
//public class ShiftedPlayer extends Player {
//
//	public ShiftedPlayer(Player wrappedPlayer, BlockPos fakePosition) {
//		super(wrappedPlayer.getLevel(), wrappedPlayer.getGameProfile());
//		this.wrappedPlayer = wrappedPlayer;
//		this.posX = fakePosition.getX();
//		this.posY = fakePosition.getY();
//		this.posZ = fakePosition.getZ();
//	}
//
//	Player wrappedPlayer;
//
//	@Override
//	public boolean isSpectator() {
//		return this.wrappedPlayer.isSpectator();
//	}
//
//	@Override
//	public boolean isCreative() {
//		return this.wrappedPlayer.isCreative();
//	}
//}
