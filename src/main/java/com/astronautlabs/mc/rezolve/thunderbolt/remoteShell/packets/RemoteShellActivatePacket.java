package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell.packets;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.util.RezolveByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

@RegistryId("remote_shell_activate")
public class RemoteShellActivatePacket extends RezolveMenuPacket {

	public RemoteShellActivatePacket() { }

	public RemoteShellActivatePacket(BlockPos activatedMachine, String playerId) {
		this.activatedMachine = activatedMachine;
		this.playerId = playerId;
	}

	BlockPos activatedMachine;
	String playerId;

	public BlockPos getActivatedMachine() {
		return this.activatedMachine;
	}
	public String getPlayerId() {
		return this.playerId;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);
		this.activatedMachine = RezolveByteBufUtils.readBlockPos(buf);
		this.playerId = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeBlockPos(this.activatedMachine);
		buf.writeUtf(this.playerId);
	}
}
