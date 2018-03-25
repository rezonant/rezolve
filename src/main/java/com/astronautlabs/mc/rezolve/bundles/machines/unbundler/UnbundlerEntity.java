package com.astronautlabs.mc.rezolve.bundles.machines.unbundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.util.ItemUtil;
import com.astronautlabs.mc.rezolve.util.RezolveNBT;
import com.astronautlabs.mc.rezolve.core.inventory.InventorySnapshot;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.Operation;
import com.astronautlabs.mc.rezolve.core.inventory.VirtualInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class UnbundlerEntity extends MachineEntity {
	public UnbundlerEntity() {
		super("unbundler_tile_entity");
		
		this.updateInterval = 5;
		this.maxEnergyStored = 50000;
	}
	
    private int unbundleEnergyCost = 1000;

	@Override
	public int getSizeInventory() {
		return 25;
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (this.isInputSlot(index))
			return stack.getItem() == RezolveMod.BUNDLE_ITEM;
		
		return false;
	}
	
	public boolean isOutputSlot(int index) {
		return index >= 10 && index <= 25;
	}
	
	public boolean isInputSlot(int index) {
		return index < 10;
	}
	
	private VirtualInventory dummyInventory = new VirtualInventory();
	
	private boolean distributeItem(ItemStack stack) {
		stack = stack.copy();
		
		do {
			// If this gets assigned, we'll attempt to do a second pass, used to assign parts of a stack 
			// across multiple existing stacks.
			
			boolean reassignRest = false;
			
			// Find a slot with an existing stack to join this stack into
			
			for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
				if (!this.isOutputSlot(i))
					continue;
				
				ItemStack existingStack = this.getStackInSlot(i);
				
				if (existingStack == null)
					continue;
				
				if (!ItemUtil.areStacksSame(existingStack, stack))
					continue;
				
				int maxStackSize = stack.getItem().getItemStackLimit(stack);
				
				if (existingStack.stackSize + stack.stackSize > maxStackSize) {
					stack.stackSize = existingStack.stackSize + stack.stackSize - maxStackSize;
					existingStack.stackSize = maxStackSize;
					
					// Try to reassign the rest in the next pass
					reassignRest = true;
					break;
				} else {
					existingStack.stackSize += stack.stackSize;
				}
				
				// We have assigned the entirety of the stack
				return true;
			}

			// Continue, so that we try to assign the rest of the stack elsewhere.
			if (reassignRest)
				continue;
			
			// Find an empty slot for the stack
			
			for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
				if (!this.isOutputSlot(i))
					continue;
				
				ItemStack existingStack = this.getStackInSlot(i);
				
				if (existingStack != null && existingStack.stackSize > 0)
					continue;
				
				// TODO: should we bother checking getItemStackLimit() here? 
				
				this.setInventorySlotContents(i, stack);
				
				// We have assigned the entirety of the stack
				return true;
			}
		} while (false);
		
		return false;
	}

	protected boolean allowedToPullFrom(int slot) {
		return this.isOutputSlot(slot);
	}
	
	protected boolean allowedToPushTo(int slot) {
		return this.isInputSlot(slot);
	}
	
	private void unpackBundle() {
		if (this.hasCurrentOperation())
			return;
		
		if (this.storedEnergy < this.unbundleEnergyCost)
			return;
		
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
		
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (inputStack == null || inputStack.stackSize == 0) {
				continue;
			}
		
			// OK, this bundle can be unbundled!
			
			this.startOperation(new UnbundlerOperation(this, inputStack));
		}
	}
	
	public int getSlotWithBundle(ItemStack bundle) {
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
			
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (!ItemUtil.areStacksSame(inputStack, bundle))
				continue;
			
			return i;
		}
		
		return -1;
	}
	
	public boolean attemptUnbundle(ItemStack bundle) {

		if (bundle == null)
			return true;
		
		int slot = this.getSlotWithBundle(bundle);
		
		if (slot < 0)
			return false;
		
		ItemStack realStack = this.getStackInSlot(slot);
		
		if (!bundle.hasTagCompound()) {
			// This implies we have a bundle that has no items in it. 
			// In some respects the correct action is to consume the bundle.
			// Also, because this should never happen and the user may have cheated in the bundle 
			// to fuck with us, we're just gonna take it.
			realStack.stackSize -= 1;
			
			if (realStack.stackSize <= 0) {
				this.setInventorySlotContents(slot, null);
			}
			
			return true;
		}
		
		NBTTagCompound nbt = bundle.getTagCompound();

		this.dummyInventory.clear();
		RezolveNBT.readInventory(nbt, this.dummyInventory);
		
		// Take a snapshot of the inventory, in the case where we cannot 
		// distribute all the items of the bundle, we will roll back this "transaction"
		// and report failure which will cause the underlying unbundler operation to 
		// hold until inventory slots are cleared up.
		
		InventorySnapshot snapshot = this.startInventoryTransaction();
		boolean cancelled = false;
		
		for (ItemStack outputItem : this.dummyInventory.getStacks()) {
			if (!this.distributeItem(outputItem)) {
				cancelled = true;
				continue;
			}
		}
		
		if (cancelled) {
			this.rollbackInventoryTransaction(snapshot);
			return false;
		}
		
		this.commitInventoryTransaction(snapshot);
		
		// Consume the bundle
		
		realStack.stackSize -= 1;
		if (realStack.stackSize == 0)
			this.setInventorySlotContents(slot, null);
		
		return true;
	}
	
	@Override
	public void updatePeriodically() {
		this.unpackBundle();
	}

	@Override
	public Operation createOperation() {
		return new UnbundlerOperation(this);
	}
	
	public boolean hasBundle(ItemStack bundle) {
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
		
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (!ItemUtil.areStacksSame(inputStack, bundle)) {
				continue;
			}
			
			return true;
		}
		
		return false;
	}
}
