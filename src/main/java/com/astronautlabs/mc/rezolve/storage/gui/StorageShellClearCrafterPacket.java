package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class StorageShellClearCrafterPacket extends RezolveMenuPacket {

	public StorageShellClearCrafterPacket() {
	}

	public StorageShellClearCrafterPacket(Player player) {
		this.playerId = player.getStringUUID();
	}

	public String playerId;

	@Override
	public void read(FriendlyByteBuf buf) {
		this.playerId = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.playerId);
	}
}
