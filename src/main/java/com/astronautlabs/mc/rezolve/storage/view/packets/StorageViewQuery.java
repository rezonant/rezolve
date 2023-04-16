package com.astronautlabs.mc.rezolve.storage.view.packets;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

@RegistryId("storage_view_query")
public class StorageViewQuery extends StorageViewPacket {

	public StorageViewQuery() {

	}

	public StorageViewQuery(Player player, String query, int offset, int limit) {
		this.playerId = player.getStringUUID();
		this.query = query;
		this.offset = offset;
		this.limit = limit;
	}

	public String playerId;
	public String query;
	public int offset;
	public int limit;

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);
		this.playerId = buf.readUtf();
		this.query = buf.readUtf();
		this.offset = buf.readInt();
		this.limit = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeUtf(this.playerId);
		buf.writeUtf(this.query);
		buf.writeInt(this.offset);
		buf.writeInt(this.limit);
	}
}
