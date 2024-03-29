package com.rezolvemc.storage.gui;

import com.rezolvemc.common.network.RezolveMenuPacket;
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
