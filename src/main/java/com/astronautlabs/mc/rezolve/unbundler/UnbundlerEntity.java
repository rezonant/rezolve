package com.astronautlabs.mc.rezolve.unbundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.BundlerNBT;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class UnbundlerEntity extends MachineEntity {
	public UnbundlerEntity() {
		super("unbundler_tile_entity");
		
		this.updateInterval = 20 * 2;
		this.maxEnergyStored = 20000;
	}
	
    private int unbundleEnergyCost = 1000;

	@Override
	public int getSizeInventory() {
		return 25;
	}
	
	@Override
	public boolean allowInputToSlot(int index) {
		if (this.isOutputSlot(index))
			return false;
		return true;
	}

	public boolean isOutputSlot(int index) {
		return index >= 10 && index <= 25;
	}
	
	public boolean isInputSlot(int index) {
		return index < 10;
	}
	
	private VirtualInventory dummyInventory = new VirtualInventory();
	
	private boolean distributeItem(ItemStack stack, boolean simulate) {
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isOutputSlot(i))
				continue;
			
			ItemStack existingStack = this.getStackInSlot(i);
			
			if (existingStack == null)
				continue;
			
			if (!RezolveMod.areStacksSame(existingStack, stack))
				continue;
			
			if (!simulate)
				existingStack.stackSize += stack.stackSize;
			
			return true;
		}
		
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isOutputSlot(i))
				continue;
			
			ItemStack existingStack = this.getStackInSlot(i);
			
			if (existingStack != null && existingStack.stackSize > 0)
				continue;
			
			if (!simulate)
				this.setInventorySlotContents(i, stack.copy());
			
			return true;
		}
		
		return false;
	}
	
	private void unpackBundle() {
		if (this.storedEnergy < this.unbundleEnergyCost)
			return;
		
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
		
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (inputStack == null || inputStack.stackSize == 0) {
				continue;
			}
			
			System.out.println("Located a bundle to unbundle");
		
			// OK, this bundle can be unbundled!
			
			NBTTagCompound nbt = inputStack.getTagCompound();

			if (nbt == null) {
				// This implies we have a bundle that has no items in it. 
				// In some respects the correct action is to consume the bundle.
				// Also, because this should never happen and the user may have cheated in the bundle 
				// to fuck with us, we're just gonna take it.
				inputStack.stackSize -= 1;
				
				if (inputStack.stackSize <= 0) {
					this.setInventorySlotContents(i, null);
				}
				
				continue;
			}
			
			this.dummyInventory.clear();
			
			BundlerNBT.readInventory(nbt, this.dummyInventory);
			boolean cancelled = false;

			for (ItemStack outputItem : this.dummyInventory.getStacks()) {
				if (!this.distributeItem(outputItem, true)) {
					System.out.println("Cancelled, can't distribute item stack of "+outputItem.getItem().getRegistryName());
					cancelled = true;
					continue;
				}
			}
			
			if (cancelled) {
				System.out.println("Unbundle cancelled");
				continue;
			}

			for (ItemStack outputItem : this.dummyInventory.getStacks()) {
				this.distributeItem(outputItem, false);
			}
			
			// Consume the bundle
			
			System.out.println("Consume the bundle");
			inputStack.stackSize -= 1;
			
			if (inputStack.stackSize == 0)
				this.setInventorySlotContents(i, null);
			
			// Consume the power 
			
			this.storedEnergy -= this.unbundleEnergyCost;
			
			break;
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (this.isInputSlot(index))
			return true;
	
		if (this.isOutputSlot(index))
			return false; 
		
		return true;
	}

	@Override
	public void updatePeriodically() {
		unpackBundle();
	}
	
	@Override
	protected boolean allowedToPullFrom(int slot) {
		return this.isOutputSlot(slot);
	}
	
	@Override
	protected boolean allowedToPushTo(int slot) {
		return this.isInputSlot(slot);
	}
}
