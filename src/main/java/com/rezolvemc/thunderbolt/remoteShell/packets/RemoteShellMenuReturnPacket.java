package com.rezolvemc.thunderbolt.remoteShell.packets;

import org.torchmc.RezolveMenuPacket;
import net.minecraft.network.FriendlyByteBuf;

public class RemoteShellMenuReturnPacket extends RezolveMenuPacket {

	public RemoteShellMenuReturnPacket() { }

	public RemoteShellMenuReturnPacket(String playerId) {
		this.playerId = playerId;
	}

	String playerId;

	public String getPlayerId() {
		return this.playerId;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		this.playerId = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.playerId);
	}

}
