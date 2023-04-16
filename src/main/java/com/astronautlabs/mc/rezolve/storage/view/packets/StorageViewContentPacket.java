package com.astronautlabs.mc.rezolve.storage.view.packets;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

@RegistryId("storage_view_content")
public class StorageViewContentPacket extends RezolveMenuPacket {

	public int totalStackCount;
	public int totalItemsCount;
	public int startIndex;

	public void setItems(List<ItemStack> items) {
		itemEntries = new ArrayList<>();

		for (ItemStack item : items) {
			itemEntries.add(new ItemEntry(item));
		}
	}

	public List<ItemEntry> itemEntries;

	public static class ItemEntry {
		public ItemEntry(ItemStack stack) {
			this.stack = stack;
			this.amount = stack.getCount();
			this.hash = ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack));
		}

		public ItemStack stack;
		public int amount;
		public String hash;
	}

	@Override
	public void read(FriendlyByteBuf buf) {
		super.read(buf);

		this.totalStackCount = buf.readInt();
		this.totalItemsCount = buf.readInt();
		this.startIndex = buf.readInt();
		this.itemEntries = new ArrayList<>();

		int itemCount = buf.readInt();
		for (int i = 0; i < itemCount; ++i) {
			ItemEntry item = new ItemEntry(buf.readItem());
			item.amount = buf.readInt();
			item.hash = buf.readUtf();

			this.itemEntries.add(item);
		}
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);

		buf.writeInt(this.totalStackCount);
		buf.writeInt(this.totalItemsCount);
		buf.writeInt(startIndex);

		buf.writeInt(this.itemEntries.size());
		for (ItemEntry item : this.itemEntries) {
			buf.writeItem(item.stack);
			buf.writeInt(item.amount);
			buf.writeUtf(item.hash);
		}
	}
}
