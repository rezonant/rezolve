package com.astronautlabs.mc.rezolve.machines.diskManipulator;

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
	public List<ItemStack> items;

	@Override
	public void fromBytes(ByteBuf buf) {
		this.totalStackCount = buf.readInt();
		this.totalItemsCount = buf.readInt();
		this.startIndex = buf.readInt();
		this.items = new ArrayList<>();

		int itemCount = buf.readInt();
		for (int i = 0; i < itemCount; ++i) {
			ItemStack item = ByteBufUtils.readItemStack(buf);
			item.stackSize = buf.readInt();
			this.items.add(item);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.totalStackCount);
		buf.writeInt(this.totalItemsCount);
		buf.writeInt(startIndex);

		buf.writeInt(this.items.size());
		for (ItemStack item : this.items) {
			ByteBufUtils.writeItemStack(buf, item);
			buf.writeInt(item.stackSize);
		}
	}
}
