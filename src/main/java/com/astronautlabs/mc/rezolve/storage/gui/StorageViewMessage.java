package com.astronautlabs.mc.rezolve.storage.gui;

import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class StorageViewMessage implements IMessage {

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
			this.amount = stack.stackSize;
			this.hash = ItemStackUtil.hashOfStack(ItemStackUtil.getSingleItem(stack));
		}

		public ItemStack stack;
		public int amount;
		public String hash;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.totalStackCount = buf.readInt();
		this.totalItemsCount = buf.readInt();
		this.startIndex = buf.readInt();
		this.itemEntries = new ArrayList<>();

		int itemCount = buf.readInt();
		for (int i = 0; i < itemCount; ++i) {
			ItemEntry item = new ItemEntry(ByteBufUtils.readItemStack(buf));
			item.amount = buf.readInt();
			item.hash = ByteBufUtils.readUTF8String(buf);

			this.itemEntries.add(item);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.totalStackCount);
		buf.writeInt(this.totalItemsCount);
		buf.writeInt(startIndex);

		buf.writeInt(this.itemEntries.size());
		for (ItemEntry item : this.itemEntries) {
			ByteBufUtils.writeItemStack(buf, item.stack);
			buf.writeInt(item.amount);
			ByteBufUtils.writeUTF8String(buf, item.hash);
		}
	}
}
