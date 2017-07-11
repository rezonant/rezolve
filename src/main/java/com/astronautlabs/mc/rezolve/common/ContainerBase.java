package com.astronautlabs.mc.rezolve.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public abstract class ContainerBase<T extends TileEntity> extends Container {

	public ContainerBase(T entity) {
		super();
		
		this.entity = entity;
		this.inventory = (IInventory)entity;
		
	}
	
	protected T entity;
	protected IInventory inventory;
	
	public T getEntity() {
		return this.entity;
	}
    
}