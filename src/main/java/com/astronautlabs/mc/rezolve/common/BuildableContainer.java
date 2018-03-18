package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.inventory.GhostSlot;
import com.astronautlabs.mc.rezolve.inventory.ValidatedSlot;
import com.astronautlabs.mc.rezolve.machines.MachineOutputSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class BuildableContainer<T extends TileEntity & IInventory> extends ContainerBase<T> {

	public BuildableContainer(T entity, Slot[] slots) {
		super(entity);

		for (Slot slot : slots)
			this.addSlotToContainer(slot);

	}

	public static <T extends TileEntity & IInventory> Builder<T> withEntity(T entity) {
		return new Builder<T>(entity);
	}

	public static abstract class SlotFactory {
		public abstract Slot create(IInventory inventory, int index, int xPosition, int yPosition);
	}

	public static class Builder<T extends TileEntity & IInventory> {
		public Builder(T entity) {
			this.entity = entity;
		}

		T entity = null;
		List<Slot> slots = new ArrayList<Slot>();
		int inventorySlotSize;

		public Builder slotSize(int inventorySlotSize) {
			this.inventorySlotSize = inventorySlotSize;
			return this;
		}

		public Builder addGhostSlot(IInventory inventory, int index, int xPosition, int yPosition) {
			this.slots.add(new GhostSlot(inventory, index, xPosition, yPosition));
			return this;
		}

		public Builder addGhostSlot(int index, int xPosition, int yPosition) {
			this.slots.add(new GhostSlot(this.entity, index, xPosition, yPosition));
			return this;
		}

		public Builder addOutputSlot(IInventory inventory, int index, int xPosition, int yPosition) {
			this.slots.add(new MachineOutputSlot(inventory, index, xPosition, yPosition));
			return this;
		}

		public Builder addSlot(IInventory inventory, int index, int xPosition, int yPosition) {
			this.slots.add(new Slot(inventory, index, xPosition, yPosition));
			return this;
		}

		public Builder addSlot(int index, int xPosition, int yPosition) {
			this.slots.add(new Slot(this.entity, index, xPosition, yPosition));
			return this;
		}

		public Builder addSlotGrid(IInventory inventory, SlotFactory slotConstructor, int firstSlotIndex, int xPos, int yPos, int gridWidth, int gridHeight) {
			for (int y = 0; y < gridHeight; ++y) {
				for (int x = 0; x < gridWidth; ++x) {
					this.slots.add(slotConstructor.create(inventory, firstSlotIndex + x + y * gridWidth, xPos + x * this.inventorySlotSize, yPos + y * this.inventorySlotSize));
				}
			}
			return this;
		}

		public Builder addPlayerSlots(IInventory playerInv, int xPos, int yPos) {
			return this.addPlayerSlots(playerInv, xPos, yPos, 0, 58);
		}

		public Builder addPlayerSlots(IInventory playerInv, int xPos, int yPos, int hotbarX, int hotbarY) {
			// Player Inventory, slots 9-35
			for (int y = 0; y < 3; ++y) {
				for (int x = 0; x < 9; ++x)
					this.slots.add(new Slot(playerInv, 9 + x + y * 9, xPos + x * this.inventorySlotSize, yPos + y * inventorySlotSize));
			}

			// Player Hotbar, slots 0-8
			for (int x = 0; x < 9; ++x)
				this.slots.add(new Slot(playerInv, x, xPos + hotbarX + x * this.inventorySlotSize, yPos + hotbarY));

			return this;
		}

		public Builder addOutputSlotGrid(IInventory inventory, int firstSlotIndex, int xPos, int yPos, int gridWidth, int gridHeight) {
			for (int y = 0; y < gridHeight; ++y) {
				for (int x = 0; x < gridWidth; ++x) {
					this.slots.add(new MachineOutputSlot(inventory, firstSlotIndex + x + y * gridWidth, xPos + x * this.inventorySlotSize, yPos + y * this.inventorySlotSize));
				}
			}

			return this;
		}

		public Builder addValidatedSlotGrid(int firstSlotIndex, ValidatedSlot.Validator validator, int x, int y, int gridWidth, int gridHeight) {
			this.addValidatedSlotGrid(this.entity, firstSlotIndex, validator, x, y, gridWidth, gridHeight);
			return this;
		}

		public Builder addValidatedSlotGrid(IInventory inventory, int firstSlotIndex, ValidatedSlot.Validator validator, int xPos, int yPos, int gridWidth, int gridHeight) {
			for (int y = 0; y < gridHeight; ++y) {
				for (int x = 0; x < gridWidth; ++x) {
					this.slots.add(new ValidatedSlot(inventory, firstSlotIndex + x + y * gridWidth, xPos + x * this.inventorySlotSize, yPos + y * this.inventorySlotSize, validator));
				}
			}

			return this;
		}

		public Builder addOutputSlotGrid(int firstSlotIndex, int x, int y, int gridWidth, int gridHeight) {
			this.addOutputSlotGrid(this.entity, firstSlotIndex, x, y, gridWidth, gridHeight);
			return this;
		}

		public Builder addGhostSlotGrid(IInventory inventory, int firstSlotIndex, int xPos, int yPos, int gridWidth, int gridHeight) {
			for (int y = 0; y < gridHeight; ++y) {
				for (int x = 0; x < gridWidth; ++x) {
					this.slots.add(new GhostSlot(inventory, firstSlotIndex + x + y * gridWidth, xPos + x * this.inventorySlotSize, yPos + y * this.inventorySlotSize));
				}
			}
			return this;
		}

		public Builder addSlotGrid(IInventory inventory, int firstSlotIndex, int xPos, int yPos, int gridWidth, int gridHeight) {
			for (int y = 0; y < gridHeight; ++y) {
				for (int x = 0; x < gridWidth; ++x) {
					this.slots.add(new Slot(inventory, firstSlotIndex + x + y * gridWidth, xPos + x * this.inventorySlotSize, yPos + y * this.inventorySlotSize));
				}
			}
			return this;
		}

		public Builder addSlotGrid(int firstSlotIndex, int xPos, int yPos, int gridWidth, int gridHeight) {
			this.addSlotGrid(this.entity, firstSlotIndex, xPos, yPos, gridWidth, gridHeight);
			return this;
		}

		public Builder addSlot(Slot slot) {
			this.slots.add(slot);
			return this;
		}

		public T getEntity() {
			return this.entity;
		}

		public BuildableContainer<T> build() {
			return new BuildableContainer<T>(this.entity, this.slots.toArray(new Slot[this.slots.size()]));
		}
	}

}