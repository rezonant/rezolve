package com.astronautlabs.mc.rezolve.common;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class BundlerNBT {
	public static void writeInventory(NBTTagCompound nbt, IInventory inventory) {
		writeInventory(nbt, inventory, 0, inventory.getSizeInventory());
	}
	
	public static void writeInventory(NBTTagCompound nbt, IInventory inventory, int startSlot, int slotCount) {
	    NBTTagList list = new NBTTagList();
	    for (int i = startSlot; i < slotCount; ++i) {
	        if (inventory.getStackInSlot(i) != null) {
	            NBTTagCompound stackTag = new NBTTagCompound();
	            stackTag.setByte("Slot", (byte) i);
	            inventory.getStackInSlot(i).writeToNBT(stackTag);
	            list.appendTag(stackTag);
	        }
	    }
	    nbt.setTag("Items", list);

	}
	
	public static void readInventory(NBTTagCompound nbt, IInventory inventory) {
	    NBTTagList list = nbt.getTagList("Items", 10);
	    for (int i = 0; i < list.tagCount(); ++i) {
	        NBTTagCompound stackTag = list.getCompoundTagAt(i);
	        int slot = stackTag.getByte("Slot") & 255;
	        
	        inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(stackTag));
	    }
	}
}
