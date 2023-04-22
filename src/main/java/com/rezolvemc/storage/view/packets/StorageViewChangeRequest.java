package com.rezolvemc.storage.view.packets;

import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@RegistryId("storage_view_change_request")
public class StorageViewChangeRequest extends StorageViewPacket {

	public static final String OPERATION_TAKE = "t";
	public static final String OPERATION_GIVE = "g";

	public StorageViewChangeRequest() {
	}

	public static StorageViewChangeRequest takeItems(AbstractContainerMenu menu, Player player, ItemStack stack, String hash) {
		StorageViewChangeRequest message = new StorageViewChangeRequest();
		message.setMenu(menu);
		message.operationId = UUID.randomUUID().toString();
		message.playerId = player.getStringUUID();
		message.operationType = OPERATION_TAKE;
		message.requestedStackHash = hash;
		message.requestedStack = stack;

		return message;
	}

	public static StorageViewChangeRequest giveItems(AbstractContainerMenu menu, Player player, ItemStack stack, String hash) {
		StorageViewChangeRequest message = new StorageViewChangeRequest();
		message.setMenu(menu);
		message.operationId = UUID.randomUUID().toString();
		message.playerId = player.getStringUUID();
		message.operationType = OPERATION_GIVE;
		message.requestedStackHash = hash;
		message.requestedStack = stack;

		return message;
	}

	public String playerId;
	public String operationId;
	public String operationType;
	public ItemStack requestedStack;
	public String requestedStackHash;

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);

		this.playerId = buf.readUtf();
		this.operationId = buf.readUtf();
		this.operationType = buf.readUtf();
		this.requestedStack = buf.readItem();
		this.requestedStackHash = buf.readUtf();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);

		buf.writeUtf(this.playerId);
		buf.writeUtf(this.operationId);
		buf.writeUtf(this.operationType);
		buf.writeItem(this.requestedStack);
		buf.writeUtf(this.requestedStackHash);
	}
}
