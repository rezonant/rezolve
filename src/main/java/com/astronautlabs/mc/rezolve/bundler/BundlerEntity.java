package com.astronautlabs.mc.rezolve.bundler;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity.ItemMemo;
import com.astronautlabs.mc.rezolve.common.RezolveNBT;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.common.Operation;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class BundlerEntity extends MachineEntity {
	public BundlerEntity() {
		super("bundler_tile_entity");
	    this.updateInterval = 5;
	    this.maxEnergyStored = 50000;
	    
	}

	class ItemMemo {
		ItemMemo(int index, ItemStack stack) {
			this.index = index;
			this.stack = stack;
		}
		
		public int index;
		public ItemStack stack;
	}
	
    private int bundleEnergyCost = 1000;
    
	@Override
	public int getSizeInventory() {
		return 27;
	}

	public boolean isOutputSlot(int index) {
		return index >= 18 && index <= 26;
	}
	
	public boolean isPatternSlot(int index) {
		return index >= 9 && index <= 17;
	}
	
	public boolean isInputSlot(int index) {
		return index < 9;
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

	private boolean startProducing(ItemStack pattern) {
		if (!pattern.hasTagCompound()) {
			System.out.println("Bundler: Invalid pattern: No NBT...");
			return false;
		}
		
		ArrayList<ItemMemo> selectedItems = this.selectItemsFor(pattern);

		// If we don't have the correct amount of items, then we don't have enough to make a bundle.
		if (selectedItems == null) {
			return false;
		}

		this.startOperation(new BundlerOperation(this, pattern));
		return true;
	}

	@Override
	public Operation createOperation() {
		return new BundlerOperation(this);
	}
	
	public ItemStack makeBundleStack(ItemStack pattern) {
		
		int dye = 0;
		if (pattern.hasTagCompound()) {
			NBTTagCompound nbt = pattern.getTagCompound();
			if (nbt.hasKey("Color"))
				dye = nbt.getInteger("Color") + 1;
		}
		
		ItemStack bundleStack = new ItemStack(RezolveMod.BUNDLE_ITEM, 1, dye);
		bundleStack.setTagCompound(pattern.getTagCompound());
		return bundleStack;
	}

	private VirtualInventory dummyInventory = new VirtualInventory();
	
    private void produceBundles() {
		if (this.hasCurrentOperation())
			return;
		
    	for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
    		if (!this.isPatternSlot(i))
    			continue;
    		
    		ItemStack pattern = this.getStackInSlot(i);
    		if (pattern == null || pattern.stackSize == 0)
    			continue;
    		
    		boolean started = this.startProducing(pattern);
    		
    		if (started)
    			break;
    	}
    }
    
    public boolean storeBundle(ItemStack bundle, boolean simulate) {

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
			
			if (!RezolveMod.areStacksSame(bundle, outputStack))
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

	@Override
	public int getSlots() {
		return this.getSizeInventory();
	}

	protected boolean allowedToPullFrom(int slot) {
		return this.isOutputSlot(slot);
	}
	
	protected boolean allowedToPushTo(int slot) {
		return this.isInputSlot(slot);
	}

	public boolean hasPattern(ItemStack pattern) {
		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
			ItemStack slotStack = this.getStackInSlot(i);
			
			if (slotStack == null || slotStack.stackSize <= 0)
				continue;
			
			if (RezolveMod.areStacksSame(slotStack, pattern))
				return true;
		}
		
		return false;
	}

	protected ArrayList<ItemMemo> getAvailableItems() {

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
		
		return availableItems;
	}
	
	protected ArrayList<ItemMemo> selectItemsFor(ItemStack pattern) {

		this.dummyInventory.clear();

		NBTTagCompound nbt = pattern.getTagCompound();
		RezolveNBT.readInventory(nbt, this.dummyInventory);
		
		ArrayList<ItemMemo> availableItems = this.getAvailableItems();
		ArrayList<ItemMemo> selectedItems = new ArrayList<ItemMemo>();
		
		int requestedItemCount = 0;
		int bundleDepth = 0;
		
		for (ItemStack requestedItem : this.dummyInventory.getStacks()) {
			if (requestedItem == null || requestedItem.stackSize == 0)
				continue;
		
			if (requestedItem.getItem() == RezolveMod.BUNDLE_ITEM)
				bundleDepth = Math.max(bundleDepth, BundleItem.getBundleDepth(requestedItem));
			
			int missing = requestedItem.stackSize;
			requestedItemCount += missing;
			
			for (int i = 0, max = requestedItem.stackSize; i < max; ++i) {
				for (int j = 0, maxJ = availableItems.size(); j < maxJ; ++j) {
					ItemMemo availableItem = availableItems.get(j);
					
					if (!RezolveMod.areStacksSame(availableItem.stack, requestedItem))
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
		
		if (requestedItemCount != selectedItems.size())
			return null;
		
		return selectedItems;
	}
	
	public boolean hasItemsFor(ItemStack pattern) {
		ArrayList<ItemMemo> selectedItems = this.selectItemsFor(pattern);
		return selectedItems != null;
	}

	public void completeOperation(BundlerOperation bundlerOperation) {
		
		ArrayList<ItemMemo> selectedItems = this.selectItemsFor(bundlerOperation.getPattern());
		// OK, remove the source materials
		
		for (ItemMemo selectedItem : selectedItems) {
			selectedItem.stack.stackSize -= 1;
			if (selectedItem.stack.stackSize <= 0) {
				this.setInventorySlotContents(selectedItem.index, null);
			}
		}
		
		ItemStack bundleStack = this.makeBundleStack(bundlerOperation.getPattern());
		this.storeBundle(bundleStack, false);
		
	}
}
