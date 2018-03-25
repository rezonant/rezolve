package com.astronautlabs.mc.rezolve.storage.gui;

import io.netty.buffer.ByteBuf;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageViewRecipeRequestMessage implements IMessage {

	public StorageViewRecipeRequestMessage() {

	}

	public StorageViewRecipeRequestMessage(EntityPlayer player, IRecipeLayout recipeLayout) {

		this.playerId = player.getUniqueID().toString();

		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = itemStacks.getGuiIngredients();

		List<ItemStack> ingredients = new ArrayList<>();

		for (int i = 0; i < 9; i++) {
			if (guiIngredients.containsKey(i)) {
				IGuiIngredient<ItemStack> options = guiIngredients.get(i);

				this.slots.add(new SlotOptions(i, options.getAllIngredients()));
			} else {
				this.slots.add(new SlotOptions(i, new ArrayList<>()));
			}
		}
	}

	public String playerId;
	public List<SlotOptions> slots = new ArrayList<>();

	public static class SlotOptions {
		public SlotOptions(int slot, List<ItemStack> stack) {
			this.slot = slot;
			this.itemOptions = stack;
		}

		public int slot;
		public List<ItemStack> itemOptions;
	}


	@Override
	public void fromBytes(ByteBuf buf) {
		this.slots.clear();

		this.playerId = ByteBufUtils.readUTF8String(buf);

		int slotCount = buf.readInt();
		for (int i = 0; i < slotCount; ++i) {
			int slotNumber = buf.readInt();
			int optionCount = buf.readInt();
			List<ItemStack> options = new ArrayList<>();

			for (int option = 0; option < optionCount; ++option) {
				ItemStack optionStack = ByteBufUtils.readItemStack(buf);

				options.add(optionStack);
			}


			this.slots.add(new SlotOptions(slotNumber, options));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.playerId);
		buf.writeInt(this.slots.size());
		for (SlotOptions slot : this.slots) {
			buf.writeInt(slot.slot);
			buf.writeInt(slot.itemOptions.size());
			for (ItemStack option : slot.itemOptions) {
				ByteBufUtils.writeItemStack(buf, option);
			}
		}
	}
}
