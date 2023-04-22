package com.rezolvemc.thunderbolt.remoteShell.packets;

import com.rezolvemc.common.network.RezolveMenuPacket;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.util.RezolveByteBufUtils;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@RegistryId("remote_shell_activate")
public class RemoteShellActivatePacket extends RezolveMenuPacket {

	public RemoteShellActivatePacket() { }

	public RemoteShellActivatePacket(ResourceKey<Level> level, BlockPos activatedMachine, String playerId) {
		this.level = level;
		this.activatedMachine = activatedMachine;
		this.playerId = playerId;
	}

	ResourceKey<Level> level;
	BlockPos activatedMachine;
	String playerId;

	public ResourceKey<Level> getLevel() {
		return level;
	}

	public BlockPos getActivatedMachine() {
		return this.activatedMachine;
	}
	public String getPlayerId() {
		return this.playerId;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);
		this.level = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf()));
		this.activatedMachine = RezolveByteBufUtils.readBlockPos(buf);
		this.playerId = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeUtf(level.location().toString());
		buf.writeBlockPos(activatedMachine);
		buf.writeUtf(playerId);
	}
}
