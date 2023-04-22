package com.rezolvemc.thunderbolt.remoteShell.packets;

import org.torchmc.RezolveMenuPacket;
import com.rezolvemc.common.util.RezolveByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public class RemoteShellRenameMachinePacket extends RezolveMenuPacket {

	public RemoteShellRenameMachinePacket() { }

	public RemoteShellRenameMachinePacket(BlockPos pos, String name) {
		this.machinePos = pos;

		if (name == null)
			this.name = "";
		else
			this.name = name;
	}

	BlockPos machinePos;
	String name;

	public BlockPos getMachinePos() {
		return this.machinePos;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		this.machinePos = RezolveByteBufUtils.readBlockPos(buf);
		this.name = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		RezolveByteBufUtils.writeBlockPos(buf, this.machinePos);
		buf.writeUtf(this.name);
	}

}
