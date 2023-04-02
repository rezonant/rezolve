package com.astronautlabs.mc.rezolve.bundles.bundler;

import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.bundles.BundleItem;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.bundles.bundleBuilder.BundlePatternSlot;

import com.astronautlabs.mc.rezolve.common.inventory.OutputSlot;
import com.astronautlabs.mc.rezolve.common.inventory.VirtualInventory;
import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.common.machines.WithOperation;
import com.astronautlabs.mc.rezolve.common.util.RezolveTagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

@WithOperation(BundlerOperation.class)
public class BundlerEntity extends MachineEntity {
	public BundlerEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(BundlerEntity.class), pPos, pBlockState);
		this.updateInterval = 5;
		this.maxEnergyStored = 50000;

		int nextSlotNumber = 0;

		for (int i = 0, max = 9; i < max; ++i)
			addSlot(new Slot(this, nextSlotNumber++, 0, 0));

		for (int i = 0, max = 9; i < max; ++i)
			addSlot(new BundlePatternSlot(this, nextSlotNumber++, 0, 0));

		for (int i = 0, max = 9; i < max; ++i)
			addSlot(new OutputSlot(this, nextSlotNumber++, 0, 0));
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

	public boolean isOutputSlot(int index) {
		var slot = this.getSlot(index);
		if (slot == null)
			return false;

		return slot instanceof OutputSlot;
	}
	
	public boolean isPatternSlot(int index) {
		var slot = this.getSlot(index);
		if (slot == null)
			return false;

		return slot instanceof BundlePatternSlot;
	}
	
	public boolean isInputSlot(int index) {
		return index < 9;
	}
	
	public ItemStack[] getAvailableMaterials() 
	{
		ArrayList<ItemStack> availableMaterials = new ArrayList<ItemStack>();
		
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
			
			ItemStack ingredient = this.getStackInSlot(i);

			if (ingredient == null)
				continue;
			
			ItemStack materialStack = null;
			for (ItemStack potentialMaterialStack : availableMaterials) {
				if (RezolveMod.areStacksSame(potentialMaterialStack, ingredient)) {
					materialStack = potentialMaterialStack;
					break;
				}
			}
			
			if (materialStack == null) {
				materialStack = ingredient.copy();
				availableMaterials.add(materialStack);
			} else {
				materialStack.setCount(materialStack.getCount() + ingredient.getCount());
			}	
		}

		return availableMaterials.toArray(new ItemStack[availableMaterials.size()]);
	}
	
	public ItemStack[] getNeededMaterials()
	{
		ArrayList<ItemStack> neededMaterials = new ArrayList<ItemStack>();
		
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			if (!this.isPatternSlot(i))
				continue;
			
			ItemStack pattern = this.getStackInSlot(i);
			
			if (pattern == null)
				continue;
			
			for (ItemStack ingredient : RezolveRegistry.item(BundleItem.class).getItemsFromBundle(pattern)) {
				
				ItemStack materialStack = null;
				for (ItemStack potentialMaterialStack : neededMaterials) {
					if (RezolveMod.areStacksSame(potentialMaterialStack, ingredient)) {
						materialStack = potentialMaterialStack;
						break;
					}
				}
				
				if (materialStack == null) {
					materialStack = ingredient.copy();
					neededMaterials.add(materialStack);
				} else {
					materialStack.setCount(materialStack.getCount() + ingredient.getCount());
				}
			}	
		}

		return neededMaterials.toArray(new ItemStack[neededMaterials.size()]);
	}
	
	public ItemStack insertItem(int slotId, ItemStack stack, boolean simulate) {
		if (!this.isInputSlot(slotId) || stack == null || stack.getCount() == 0)
			return stack;
		
		ItemStack[] neededMaterials = this.getNeededMaterials();
		ItemStack[] availableMaterials = this.getAvailableMaterials();
		ItemStack neededMaterialStack = null;
		ItemStack availableMaterialStack = null;

		for (ItemStack neededMaterial : neededMaterials) {
			if (RezolveMod.areStacksSame(neededMaterial, stack)) {
				neededMaterialStack = neededMaterial;
				break;
			}
		}
		
		for (ItemStack availableMaterial : availableMaterials) {
			if (RezolveMod.areStacksSame(availableMaterial, stack)) {
				availableMaterialStack = availableMaterial;
				break;
			}
		}
		
		if (neededMaterialStack == null)
			return stack;
		
		int availableItemCount = 0;
		
		if (availableMaterialStack != null)
			availableItemCount = availableMaterialStack.getCount();
		
		if (availableItemCount >= neededMaterialStack.getCount())
			return stack;
		
		if (availableItemCount + stack.getCount() > neededMaterialStack.getCount()) {
			stack = stack.copy();
			ItemStack remainder = stack.split(stack.getCount() - (neededMaterialStack.getCount() - availableItemCount));
			ItemStack secondRemainder = super.insertItem(slotId, stack, simulate);
			
			if (secondRemainder != null) {
				remainder.setCount(remainder.getCount() + secondRemainder.getCount());
			}
			
			return remainder;
		} else {
			return super.insertItem(slotId, stack, simulate);
		}
	}

	private boolean startProducing(ItemStack pattern) {
		if (!pattern.hasTag()) {
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

	public ItemStack makeBundleStack(ItemStack pattern) {
		
		String dye = null;
		ItemStack bundleStack;
		if (pattern.hasTag()) {
			CompoundTag nbt = pattern.getTag();
			if (nbt.contains("Color"))
				dye = nbt.getString("Color");
		}

		if (dye != null)
			bundleStack = new ItemStack(BundleItem.withColor(dye), 1);
		else
			bundleStack = new ItemStack(RezolveRegistry.item(BundleItem.class), 1);

		bundleStack.setTag(pattern.getTag());
		return bundleStack;
	}

	private VirtualInventory dummyInventory = new VirtualInventory();

	@Override
	public Component getMenuTitle() {
		return Component.literal("Bundler");
	}

	private void produceBundles() {
		if (this.hasCurrentOperation())
			return;
		
    	for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
    		if (!this.isPatternSlot(i))
    			continue;
    		
    		ItemStack pattern = this.getStackInSlot(i);
    		if (pattern == null || pattern.getCount() == 0)
    			continue;
    		
    		boolean started = this.startProducing(pattern);
    		
    		if (started)
    			break;
    	}
    }
    
    public boolean storeBundle(ItemStack bundle, boolean simulate) {

		ItemStack existingOutputStack = null;
		int firstExistingSlot = -1;
		int firstEmptySlot = -1;
		
		for (int j = 0, maxJ = this.getSlotCount(); j < maxJ; ++j) {
			if (!this.isOutputSlot(j))
				continue;
			
			ItemStack outputStack = this.getStackInSlot(j);
			if (outputStack == null || outputStack.isEmpty()) {
				if (firstEmptySlot < 0)
					firstEmptySlot = j;
				
				continue;
			}
			
			if (!RezolveMod.areStacksSame(bundle, outputStack) || outputStack.getCount() >= outputStack.getMaxStackSize())
				continue;

			existingOutputStack = outputStack;
			firstExistingSlot = j;
			break;
		}
		
		if (existingOutputStack == null) {
			if (firstEmptySlot < 0) {
				// Oh shit, we're out of space.
				return false;
			} else {
				if (!simulate)
					this.setItem(firstEmptySlot, bundle);
				return true;
			}
		} else {
			if (!simulate) {
				existingOutputStack.setCount(existingOutputStack.getCount() + 1);
				this.setItem(firstExistingSlot, existingOutputStack);
			}
			
			return true;
		}
		
    }
    
	@Override
	public void updatePeriodically() {
		this.produceBundles();
	}

	@Override
	protected boolean allowedToPullFrom(int slot) {
		return this.isOutputSlot(slot);
	}

	@Override
	protected boolean allowedToPushTo(int slot, ItemStack stack) {
		return this.isInputSlot(slot);
	}

	public boolean hasPattern(ItemStack pattern) {
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			ItemStack slotStack = this.getStackInSlot(i);
			
			if (slotStack == null || slotStack.getCount() <= 0)
				continue;
			
			if (RezolveMod.areStacksSame(slotStack, pattern))
				return true;
		}
		
		return false;
	}

	protected ArrayList<ItemMemo> getAvailableItems() {

		ArrayList<ItemMemo> availableItems = new ArrayList<ItemMemo>();

		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
			
			ItemStack slotStack = this.getStackInSlot(i);
			
			if (slotStack == null)
				continue;
			
			for (int j = 0, maxJ = slotStack.getCount(); j < maxJ; ++j)
				availableItems.add(new ItemMemo(i, slotStack));
		}
		
		return availableItems;
	}
	
	protected ArrayList<ItemMemo> selectItemsFor(ItemStack pattern) {

		this.dummyInventory.clearContent();

		CompoundTag nbt = pattern.getTag();
		RezolveTagUtil.readInventory(nbt, this.dummyInventory);
		
		ArrayList<ItemMemo> availableItems = this.getAvailableItems();
		ArrayList<ItemMemo> selectedItems = new ArrayList<ItemMemo>();
		
		int requestedItemCount = 0;
		int bundleDepth = 0;
		
		for (ItemStack requestedItem : this.dummyInventory.getStacks()) {
			if (requestedItem == null || requestedItem.getCount() == 0)
				continue;
		
			if (requestedItem.getItem() == RezolveRegistry.item(BundleItem.class))
				bundleDepth = Math.max(bundleDepth, BundleItem.getBundleDepth(requestedItem));
			
			int missing = requestedItem.getCount();
			requestedItemCount += missing;
			
			for (int i = 0, max = requestedItem.getCount(); i < max; ++i) {
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
			selectedItem.stack.setCount(selectedItem.stack.getCount() - 1);
			if (selectedItem.stack.getCount() <= 0) {
				this.setItem(selectedItem.index, null);
			}
		}
		
		ItemStack bundleStack = this.makeBundleStack(bundlerOperation.getPattern());
		this.storeBundle(bundleStack, false);
		
	}
}
