package com.rezolvemc.common.util;

import com.rezolvemc.common.inventory.ContainerItemHandler;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.items.IItemHandler;

public class RezolveTagUtil {
	public static void writeInventory(CompoundTag nbt, IItemHandler inventory) {
		writeInventory(nbt, inventory, 0, inventory.getSlots());
	}

	public static void writeInventory(CompoundTag nbt, Container inventory) {
		writeInventory(nbt, new ContainerItemHandler(inventory));
	}

	public static void writeInventory(CompoundTag nbt, Container inventory, int startSlot, int slotCount) {
		writeInventory(nbt, new ContainerItemHandler(inventory));
	}

	public static void writeInventory(CompoundTag nbt, IItemHandler inventory, int startSlot, int slotCount) {
	    ListTag list = new ListTag();
	    for (int i = startSlot; i < startSlot + slotCount; ++i) {
	        if (inventory.getStackInSlot(i) != null) {
	            CompoundTag stackTag = new CompoundTag();
	            stackTag.putByte("Slot", (byte) i);
				stackTag.put("Stack", inventory.getStackInSlot(i).serializeNBT());
	            list.add(stackTag);
	        }
	    }
	    nbt.put("Items", list);
	}

	public static void readInventory(CompoundTag nbt, Container inventory) {
		readInventory(nbt, new ContainerItemHandler(inventory));
	}

	public static void readInventory(CompoundTag nbt, IItemHandler inventory) {
		if (nbt == null)
			return;
		
		if (!nbt.contains("Items"))
			return;
		
	    ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
	    for (int i = 0; i < list.size(); ++i) {
	        CompoundTag stackTag = list.getCompound(i);
	        int slot = stackTag.getByte("Slot") & 255;
	        
	        inventory.insertItem(slot, ItemStack.of(stackTag.getCompound("Stack")), false);
	    }
	}
}
