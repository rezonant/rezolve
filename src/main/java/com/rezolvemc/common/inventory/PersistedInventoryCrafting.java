// TODO: what is this for
//package com.rezolvemc.common.inventory;
//
//import net.minecraft.world.Container;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.CraftingContainer;
//import net.minecraft.world.item.ItemStack;
//
//import javax.annotation.Nullable;
//
//public class PersistedInventoryCrafting extends CraftingContainer {
//	public PersistedInventoryCrafting(Container inventory, int baseIndex, AbstractContainerMenu eventHandlerIn, int width, int height) {
//		super(eventHandlerIn, width, height);
//		this.inventory = inventory;
//		this.baseIndex = baseIndex;
//	}
//
//	private Container inventory;
//	private int baseIndex = 0;
//
//	@Override
//	public void setItem(int index, ItemStack stack) {
//		super.setItem(index, stack);
//		this.inventory.setItem(this.baseIndex + index, stack);
//	}
//
//	@Override
//	public ItemStack removeItem(int index, int count) {
//		var result = super.removeItem(index, count);
//
//		this.inventory.setItem(this.baseIndex + index, this.getItem(index));
//
//		return result;
//	}
//}
