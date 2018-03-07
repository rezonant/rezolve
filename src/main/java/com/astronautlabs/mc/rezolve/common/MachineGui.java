package com.astronautlabs.mc.rezolve.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public abstract class MachineGui<T extends MachineEntity> extends GuiContainerBase {

	protected MachineGui() {
		super(null);
	}

	public void initialize(EntityPlayer player, MachineEntity entity) {
		this.player = player;
		this.entity = (T)entity;
		this.inventorySlots = entity.createContainerFor(player);
		this.setup();
	}

	protected EntityPlayer player;
	protected T entity;

	public abstract void setup();

	public EntityPlayer getPlayer() {
		return this.player;
	}

	public IInventory getPlayerInventory() {
		return this.player.inventory;
	}

	public T getEntity() {
		return this.entity;
	}
}
