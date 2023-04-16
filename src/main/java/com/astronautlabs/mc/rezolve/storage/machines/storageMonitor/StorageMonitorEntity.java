package com.astronautlabs.mc.rezolve.storage.machines.storageMonitor;

import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.NetworkStorageAccessor;
import com.astronautlabs.mc.rezolve.thunderbolt.cable.CableNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class StorageMonitorEntity extends MachineEntity implements IStorageTileEntity {

	public StorageMonitorEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(StorageMonitorEntity.class), pPos, pBlockState);

		for (int i = 0, max = 10; i < max; ++i)
			addSlot(new Slot(this, i, 0, 0));
	}

	@Override
	public boolean hasView() {
		return true;
	}

	@Override
	public IStorageAccessor getStorageAccessor() {
		return this.getNetworkStorage();
	}

	@Override
	protected void updatePeriodicallyOnClient() {
		// Client-side
		super.updatePeriodicallyOnClient();
	}

	public NetworkStorageAccessor getNetworkStorage() {
		CableNetwork network = this.getNetwork();

		if (network != null)
			return new NetworkStorageAccessor(network);

		return null;
	}

//
//	private CraftingContainer craftMatrix = new PersistedInventoryCrafting(this, 0, new Container() {
//		@Override
//		public boolean canInteractWith(EntityPlayer playerIn) {
//			return true;
//		}
//
//		@Override
//		public void onCraftMatrixChanged(IInventory inventoryIn) {
//			super.onCraftMatrixChanged(inventoryIn);
//			StorageMonitorEntity.this.onCraftMatrixChanged();
//		}
//	}, 3, 3);
//	public IInventory craftResult = new InventoryCraftResult();
//
//	public InventoryCrafting getCraftMatrix() {
//		return this.craftMatrix;
//	}
//
//	public IInventory getCraftResult() {
//		return this.craftResult;
//	}
//
//	public void onCraftMatrixChanged() {
//		ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
//		this.craftResult.setInventorySlotContents(0, matchingRecipe);
//	}
//
//	@Override
//	public void onSlotChanged(int index, ItemStack stack) {
//		super.onSlotChanged(index, stack);
//		if (index >= 0 && index < 9)
//			this.craftMatrix.setInventorySlotContents(index, stack);
//	}

	@Override
	public void updatePeriodically() {
		super.updatePeriodically();
	}

	public boolean clearCraftingGrid() {
		IStorageAccessor accessor = this.getStorageAccessor();

		if (accessor == null)
			return false;

		for (int i = 0, max = 9; i < max; ++i) {

			ItemStack stack = this.getStackInSlot(i);

			if (stack == null)
				continue;

			ItemStack remainingStack = accessor.giveItemStack(stack, null, false);

			if (remainingStack != null && remainingStack.getCount() <= 0)
				remainingStack = null;

			this.setItem(i, remainingStack);

			if (remainingStack != null) {
				return false;
			}
		}

		return true;
	}
}
