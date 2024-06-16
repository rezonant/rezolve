package com.rezolvemc.thunderbolt.remoteShell.packets;

import com.rezolvemc.common.blocks.RezolveBlockEntityPacket;
import com.rezolvemc.common.network.RezolveMenuPacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.util.RezolveByteBufUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@RegistryId("remote_shell_rename_machine")
public class RemoteShellRenameMachinePacket extends RezolveBlockEntityPacket {

	public RemoteShellRenameMachinePacket() { }

	public RemoteShellRenameMachinePacket(ResourceKey<Level> level, BlockPos pos, String name) {
		this.level = level;
		this.machinePos = pos;

		if (name == null)
			this.name = "";
		else
			this.name = name;
	}

	ResourceKey<Level> level;
	BlockPos machinePos;
	String name;

	public BlockPos getMachinePos() {
		return this.machinePos;
	}

	public ResourceKey<Level> getLevel() {
		return level;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);
		this.level = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf()));
		this.machinePos = RezolveByteBufUtils.readBlockPos(buf);
		this.name = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeUtf(level.location().toString());
		RezolveByteBufUtils.writeBlockPos(buf, this.machinePos);
		buf.writeUtf(this.name);
	}

}
