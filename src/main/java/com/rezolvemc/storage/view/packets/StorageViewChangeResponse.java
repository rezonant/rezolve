package com.rezolvemc.storage.view.packets;

import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@RegistryId("storage_view_change_response")
public class StorageViewChangeResponse extends StorageViewPacket {

	public StorageViewChangeResponse() {
	}

	public StorageViewChangeResponse(String operationId, String operationType, Player player, ItemStack stack) {

		StorageViewChangeResponse message = new StorageViewChangeResponse();
		message.operationId = operationId;
		message.operationType = operationType;
		message.playerId = player.getStringUUID();
		message.resultingStack = stack;
	}

	public String operationId;
	public String operationType;
	public String playerId;
	public ItemStack resultingStack;

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);

		this.playerId = buf.readUtf();
		this.operationId = buf.readUtf();
		this.operationType = buf.readUtf();
		this.resultingStack = buf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);

		buf.writeUtf(this.playerId);
		buf.writeUtf(this.operationId);
		buf.writeUtf(this.operationType);
		buf.writeItem(this.resultingStack);
	}
}
