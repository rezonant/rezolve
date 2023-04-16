//package com.astronautlabs.mc.rezolve.storage.view.packets;
//
//import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import java.util.ArrayList;
//import java.util.List;
//
//public class StorageViewRecipeRequestPacket extends RezolveMenuPacket {
//
//	public StorageViewRecipeRequestPacket() {
//
//	}
//
//	public StorageViewRecipeRequestPacket(Player player, IRecipeLayout recipeLayout) {
//
//		this.playerId = player.getStringUUID();
//
//		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
//		Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = itemStacks.getGuiIngredients();
//
//		List<ItemStack> ingredients = new ArrayList<>();
//
//		for (int i = 0; i < 9; i++) {
//			if (guiIngredients.containsKey(i)) {
//				IGuiIngredient<ItemStack> options = guiIngredients.get(i);
//
//				this.slots.add(new SlotOptions(i, options.getAllIngredients()));
//			} else {
//				this.slots.add(new SlotOptions(i, new ArrayList<>()));
//			}
//		}
//	}
//
//	public String playerId;
//	public List<SlotOptions> slots = new ArrayList<>();
//
//	public static class SlotOptions {
//		public SlotOptions(int slot, List<ItemStack> stack) {
//			this.slot = slot;
//			this.itemOptions = stack;
//		}
//
//		public int slot;
//		public List<ItemStack> itemOptions;
//	}
//
//
//	@Override
//	public void read(FriendlyByteBuf buf) {
//		this.slots.clear();
//
//		this.playerId = buf.readUtf();
//
//		int slotCount = buf.readInt();
//		for (int i = 0; i < slotCount; ++i) {
//			int slotNumber = buf.readInt();
//			int optionCount = buf.readInt();
//			List<ItemStack> options = new ArrayList<>();
//
//			for (int option = 0; option < optionCount; ++option) {
//				ItemStack optionStack = buf.readItem();
//
//				options.add(optionStack);
//			}
//
//
//			this.slots.add(new SlotOptions(slotNumber, options));
//		}
//	}
//
//	@Override
//	public void write(FriendlyByteBuf buf) {
//		buf.writeUtf(this.playerId);
//		buf.writeInt(this.slots.size());
//		for (SlotOptions slot : this.slots) {
//			buf.writeInt(slot.slot);
//			buf.writeInt(slot.itemOptions.size());
//			for (ItemStack option : slot.itemOptions) {
//				buf.writeItem(option);
//			}
//		}
//	}
//}
