package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.common.registry.WithMenu;
import org.torchmc.inventory.InventorySnapshot;
import org.torchmc.inventory.OutputSlot;
import org.torchmc.inventory.VirtualInventory;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.machines.WithOperation;
import com.rezolvemc.common.util.RezolveTagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

@WithMenu(UnbundlerMenu.class)
@WithOperation(UnbundlerOperation.class)
public class UnbundlerEntity extends MachineEntity {
    private int unbundleEnergyCost = 1000;

	public UnbundlerEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(UnbundlerEntity.class), pPos, pBlockState);

		this.updateInterval = 5;
		this.setEnergyCapacity(50000);

		// 25 slots expected

		int nextSlotId = 0;
		for (int i = 0, max = 9; i < max; ++i)
			this.addSlot(new BundleSlot(this, nextSlotId++, 0, 0));

		for (int i = 0, max = 16; i < max; ++i)
			this.addSlot(new OutputSlot(this, nextSlotId++));

	}
	
	public boolean isOutputSlot(int index) {
		return index >= 10 && index <= 25;
	}
	
	public boolean isInputSlot(int index) {
		return index < 10;
	}
	
	private VirtualInventory dummyInventory = new VirtualInventory();

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.unbundler");
	}

	private boolean distributeItem(ItemStack stack) {
		stack = stack.copy();
		
		do {
			// If this gets assigned, we'll attempt to do a second pass, used to assign parts of a stack 
			// across multiple existing stacks.
			
			boolean reassignRest = false;
			
			// Find a slot with an existing stack to join this stack into
			
			for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
				if (!this.isOutputSlot(i))
					continue;
				
				ItemStack existingStack = this.getStackInSlot(i);
				
				if (existingStack == null)
					continue;
				
				if (!ItemStack.isSameItem(existingStack, stack))
					continue;
				
				int maxStackSize = stack.getItem().getMaxStackSize(stack);
				
				if (existingStack.getCount() + stack.getCount() > maxStackSize) {
					stack.setCount(existingStack.getCount() + stack.getCount() - maxStackSize);
					existingStack.setCount(maxStackSize);
					
					// Try to reassign the rest in the next pass
					reassignRest = true;
					break;
				} else {
					existingStack.setCount(existingStack.getCount() + stack.getCount());
				}
				
				// We have assigned the entirety of the stack
				return true;
			}

			// Continue, so that we try to assign the rest of the stack elsewhere.
			if (reassignRest)
				continue;
			
			// Find an empty slot for the stack
			
			for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
				if (!this.isOutputSlot(i))
					continue;
				
				ItemStack existingStack = this.getStackInSlot(i);
				
				if (existingStack != null && existingStack.getCount() > 0)
					continue;
				
				// TODO: should we bother checking getItemStackLimit() here? 
				
				this.setItem(i, stack);
				
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
	
	private void checkForBundlesToUnpack() {
		if (this.hasCurrentOperation())
			return;
		
		if (this.getStoredEnergy() < this.unbundleEnergyCost)
			return;
		
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
		
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (inputStack == null || inputStack.getCount() == 0) {
				continue;
			}
		
			// OK, this bundle can be unbundled!
			
			this.startOperation(new UnbundlerOperation(this, inputStack));
		}
	}
	
	public int getSlotWithBundle(ItemStack bundle) {
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
			
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (!ItemStack.isSameItem(inputStack, bundle))
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
		
		if (!bundle.hasTag()) {
			// This implies we have a bundle that has no items in it. 
			// In some respects the correct action is to consume the bundle.
			// Also, because this should never happen and the user may have cheated in the bundle 
			// to fuck with us, we're just gonna take it.
			realStack.setCount(realStack.getCount() - 1);
			
			if (realStack.getCount() <= 0) {
				this.setItem(slot, null);
			}
			
			return true;
		}
		
		CompoundTag nbt = bundle.getTag();

		this.dummyInventory.clearContent();
		RezolveTagUtil.readInventory(nbt, this.dummyInventory);
		
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
		
		realStack.setCount(realStack.getCount() - 1);
		if (realStack.getCount() == 0)
			this.setItem(slot, null);
		
		return true;
	}

	@Override
	public void updatePeriodically() {
		this.checkForBundlesToUnpack();
	}
	
	public boolean hasBundle(ItemStack bundle) {
		for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
			if (!this.isInputSlot(i))
				continue;
		
			ItemStack inputStack = this.getStackInSlot(i);
			
			if (!ItemStack.isSameItem(inputStack, bundle)) {
				continue;
			}
			
			return true;
		}
		
		return false;
	}
}
