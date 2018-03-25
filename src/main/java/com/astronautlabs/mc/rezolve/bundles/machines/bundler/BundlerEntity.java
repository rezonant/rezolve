package com.astronautlabs.mc.rezolve.bundles.machines.bundler;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.*;

import com.astronautlabs.mc.rezolve.core.inventory.VirtualInventory;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import com.astronautlabs.mc.rezolve.machines.MachineOperation;
import com.astronautlabs.mc.rezolve.util.ItemUtil;
import com.astronautlabs.mc.rezolve.util.RezolveNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BundlerEntity extends MachineEntity {
	public BundlerEntity() {
		super("bundler_tile_entity");
	    this.updateInterval = 5;
	    this.maxEnergyStored = 50000;
	    
	}

	public class ItemMemo {
		public ItemMemo(int index, ItemStack stack) {
			this.index = index;
			this.stack = stack;
		}
		
		public int index;
		public ItemStack stack;
	}
    
	@Override
	public int getSizeInventory() {
		return 27;
	}

	@Override
	public Class<? extends MachineGui> getGuiClass() {
		return BundlerGui.class;
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
	
	public ItemStack[] getAvailableMaterials() 
	{
		ArrayList<ItemStack> availableMaterials = new ArrayList<ItemStack>();
		
		for (int i = 0, max = this.getSlots(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
			
			ItemStack ingredient = this.getStackInSlot(i);

			if (ingredient == null)
				continue;
			
			ItemStack materialStack = null;
			for (ItemStack potentialMaterialStack : availableMaterials) {
				if (ItemUtil.areStacksSame(potentialMaterialStack, ingredient)) {
					materialStack = potentialMaterialStack;
					break;
				}
			}
			
			if (materialStack == null) {
				materialStack = ingredient.copy();
				availableMaterials.add(materialStack);
			} else {
				materialStack.stackSize += ingredient.stackSize;
			}	
		}

		return availableMaterials.toArray(new ItemStack[availableMaterials.size()]);
	}
	
	public ItemStack[] getNeededMaterials()
	{
		ArrayList<ItemStack> neededMaterials = new ArrayList<ItemStack>();
		
		for (int i = 0, max = this.getSlots(); i < max; ++i) {
			if (!this.isPatternSlot(i))
				continue;
			
			ItemStack pattern = this.getStackInSlot(i);
			
			if (pattern == null)
				continue;
			
			for (ItemStack ingredient : RezolveMod.instance().BUNDLE_ITEM.getItemsFromBundle(pattern)) {
				
				ItemStack materialStack = null;
				for (ItemStack potentialMaterialStack : neededMaterials) {
					if (ItemUtil.areStacksSame(potentialMaterialStack, ingredient)) {
						materialStack = potentialMaterialStack;
						break;
					}
				}
				
				if (materialStack == null) {
					materialStack = ingredient.copy();
					neededMaterials.add(materialStack);
				} else {
					materialStack.stackSize += ingredient.stackSize;
				}
			}	
		}

		return neededMaterials.toArray(new ItemStack[neededMaterials.size()]);
	}
	
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.isInputSlot(slot) || stack == null || stack.stackSize == 0)
			return stack;
		
		ItemStack[] neededMaterials = this.getNeededMaterials();
		ItemStack[] availableMaterials = this.getAvailableMaterials();
		ItemStack neededMaterialStack = null;
		ItemStack availableMaterialStack = null;

		for (ItemStack neededMaterial : neededMaterials) {
			if (ItemUtil.areStacksSame(neededMaterial, stack)) {
				neededMaterialStack = neededMaterial;
				break;
			}
		}
		
		for (ItemStack availableMaterial : availableMaterials) {
			if (ItemUtil.areStacksSame(availableMaterial, stack)) {
				availableMaterialStack = availableMaterial;
				break;
			}
		}
		
		if (neededMaterialStack == null)
			return stack;
		
		int availableItemCount = 0;
		
		if (availableMaterialStack != null)
			availableItemCount = availableMaterialStack.stackSize;
		
		if (availableItemCount >= neededMaterialStack.stackSize)
			return stack;
		
		if (availableItemCount + stack.stackSize > neededMaterialStack.stackSize) {
			stack = stack.copy();
			ItemStack remainder = stack.splitStack(stack.stackSize - (neededMaterialStack.stackSize - availableItemCount));
			ItemStack secondRemainder = super.insertItem(slot, stack, simulate);
			
			if (secondRemainder != null) {
				remainder.stackSize += secondRemainder.stackSize;
			}
			
			return remainder;
		} else {
			return super.insertItem(slot, stack, simulate);
		}
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
			return false;
		}
		
		ArrayList<ItemMemo> selectedItems = this.selectItemsFor(pattern);

		// If we don't have the correct amount of items, then we don't have enough to make a bundle.
		if (selectedItems == null) {
			return false;
		}

		this.startOperation(
			this.operation()
				.withItems(0, pattern)
				.requiresEnergy(BundleItem.getBundleCost(pattern))
				.requiresSeconds(3)
				.build());
		return true;
	}

	@Override
	public Operation<BundlerEntity> createOperation() {
		return this.operation().build();
	}

	private MachineOperation.Builder<BundlerEntity> operation() {
		return MachineOperation.create(this)
			.validWhile(op -> this.hasPattern(op.getStackInSlot(0)) && this.hasItemsFor(op.getStackInSlot(0)))
			.completesWith(op -> this.completeOperation(op));
	}

	public boolean completeOperation(MachineOperation<BundlerEntity> bundlerOperation) {

		// If there's no space, we'll just hold the operation up until there is space...

		if (!this.storeBundle(this.makeBundleStack(bundlerOperation.getStackInSlot(0)), true))
			return false;

		ArrayList<ItemMemo> selectedItems = this.selectItemsFor(bundlerOperation.getStackInSlot(0));
		// OK, remove the source materials

		for (ItemMemo selectedItem : selectedItems) {
			selectedItem.stack.stackSize -= 1;
			if (selectedItem.stack.stackSize <= 0) {
				this.setInventorySlotContents(selectedItem.index, null);
			}
		}

		ItemStack bundleStack = this.makeBundleStack(bundlerOperation.getStackInSlot(0));
		this.storeBundle(bundleStack, false);
		return true;
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
			
			if (!ItemUtil.areStacksSame(bundle, outputStack))
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
			
			if (ItemUtil.areStacksSame(slotStack, pattern))
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
					
					if (!ItemUtil.areStacksSame(availableItem.stack, requestedItem))
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
}
