package com.astronautlabs.mc.rezolve.bundler;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.BundlerNBT;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BundlerEntity extends MachineEntity {
	public BundlerEntity() {
		super("bundler_tile_entity");
	    this.updateInterval = 20 * 2;
	    this.maxEnergyStored = 20000;
	}
	
    private int bundleEnergyCost = 1000;
    
	@Override
	public int getSizeInventory() {
		return 30;
	}

	public boolean isOutputSlot(int index) {
		return index >= 20 && index <= 29;
	}
	
	public boolean isPatternSlot(int index) {
		return index >= 10 && index <= 19;
	}
	
	public boolean isInputSlot(int index) {
		return index < 10;
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (this.isInputSlot(index))
			return true;
	
		if (this.isOutputSlot(index))
			return false; 
	
		if (this.isPatternSlot(index))
			return false; // TODO: we could probably allow this
		
		return true;
	}

	private ItemStack createItemFromPattern(ItemStack pattern) {
		
		this.dummyInventory.clear();
		
		if (!pattern.hasTagCompound())
			return null;
		
		NBTTagCompound nbt = pattern.getTagCompound();
		BundlerNBT.readInventory(nbt, this.dummyInventory);
		
		class ItemMemo {
			ItemMemo(int index, ItemStack stack) {
				this.index = index;
				this.stack = stack;
			}
			
			public int index;
			public ItemStack stack;
		}
		
		ArrayList<ItemMemo> availableItems = new ArrayList<ItemMemo>();

		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
			
			ItemStack slotStack = this.getStackInSlot(i);
			
			if (slotStack == null)
				continue;
			
			for (int j = 0, maxJ = slotStack.stackSize; j < maxJ; ++j)
				availableItems.add(new ItemMemo(i, slotStack));
		}
		
		ArrayList<ItemMemo> selectedItems = new ArrayList<ItemMemo>();
		
		int requestedItemCount = 0;
		
		for (ItemStack requestedItem : this.dummyInventory.getStacks()) {
			if (requestedItem == null || requestedItem.stackSize == 0)
				continue;
		
			int missing = requestedItem.stackSize;
			requestedItemCount += missing;
			
			for (int i = 0, max = requestedItem.stackSize; i < max; ++i) {
				for (int j = 0, maxJ = availableItems.size(); j < maxJ; ++j) {
					ItemMemo availableItem = availableItems.get(j);
					
					if (!this.areStacksSame(availableItem.stack, requestedItem))
						continue;
					
					selectedItems.add(availableItem);
					availableItems.remove(j--);
					--maxJ;
					--missing;
					
					if (missing <= 0)
						break;
				}
				
				if (missing <= 0)
					break;
			}
		}
		
		// If we don't have the correct amount of items, then we don't have enough to make a bundle.
		if (requestedItemCount != selectedItems.size())
			return null;

		// Produce a bundle
		ItemStack bundleStack = new ItemStack(RezolveMod.bundleItem, 1);
		bundleStack.setTagCompound(pattern.getTagCompound());
		
		// Ensure we have space to store the output bundle, otherwise duck out early.
		
		if (!this.storeBundle(bundleStack, true))
			return null;
		
		// Ensure we have the power to do it
		
		if (this.storedEnergy < this.bundleEnergyCost)
			return null;
	
		// OK, remove the source materials
		
		for (ItemMemo selectedItem : selectedItems) {
			selectedItem.stack.stackSize -= 1;
			if (selectedItem.stack.stackSize <= 0) {
				this.setInventorySlotContents(selectedItem.index, null);
			}
		}
		
		// Remove the energy, too
		
		this.storedEnergy -= this.bundleEnergyCost;
		this.notifyUpdate();
		
		// Return the new bundle
		
		return bundleStack;
	}

	private boolean areStacksSame(ItemStack stackA, ItemStack stackB) {
		if (stackA == stackB)
			return true;
		
		if (stackA == null || stackB == null)
			return false;
		
		return (stackA.isItemEqual(stackB) && ItemStack.areItemStackTagsEqual(stackA, stackB));
	}
	
	private VirtualInventory dummyInventory = new VirtualInventory();
	
    private void produceBundles() {
    	for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
    		if (!this.isPatternSlot(i))
    			continue;
    	
    		ItemStack pattern = this.getStackInSlot(i);
    		if (pattern == null || pattern.stackSize == 0)
    			continue;
    		
    		ItemStack bundle = this.createItemFromPattern(pattern);
    		if (bundle != null) {
    			this.storeBundle(bundle, false);
    			
    			// Break now so we have to wait for the next cycle to produce a bundle
    			break;
    		}
    	}
    }
    
    private boolean storeBundle(ItemStack bundle, boolean simulate) {

		ItemStack existingOutputStack = null;
		int firstEmptySlot = -1;
		
		for (int j = 0, maxJ = this.getSizeInventory(); j < maxJ; ++j) {
			if (!this.isOutputSlot(j))
				continue;
			
			ItemStack outputStack = this.getStackInSlot(j);
			if (outputStack == null) {
				if (firstEmptySlot < 0)
					firstEmptySlot = j;
				
				continue;
			}
			
			if (!this.areStacksSame(bundle, outputStack))
				continue;
		
			existingOutputStack = outputStack;
		}
		
		if (existingOutputStack == null) {
			if (firstEmptySlot < 0) {
				// Oh shit, we're out of space.
				return false;
			} else {
				if (!simulate)
					this.setInventorySlotContents(firstEmptySlot, bundle);
				return true;
			}
		} else {
			if (!simulate)
				existingOutputStack.stackSize += 1;
			
			return true;
		}
		
    }
    
	@Override
	public void updatePeriodically() {
		this.produceBundles();
	}
}
