package com.rezolvemc.storage.machines.storageShell;

import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.storage.IStorageAccessor;
import com.rezolvemc.storage.IStorageTileEntity;
import com.rezolvemc.storage.NetworkStorageAccessor;
import com.rezolvemc.thunderbolt.cable.CableNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class StorageShellEntity extends MachineEntity implements IStorageTileEntity {
	public StorageShellEntity(BlockPos pos, BlockState state) {
		super(RezolveRegistry.blockEntityType(StorageShellEntity.class), pos, state);

		for (int i = 0, max = 10; i < max; ++i)
			addSlot(new Slot(this, i, 0, 0));
	}

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.storage_shell");
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
			return network.getStorageAccessor();

		return null;
	}


//	private CraftingContainer craftMatrix = new PersistedInventoryCrafting(this, 0, new Container() {
//		@Override
//		public boolean canInteractWith(EntityPlayer playerIn) {
//			return true;
//		}
//
//		@Override
//		public void onCraftMatrixChanged(IInventory inventoryIn) {
//			super.onCraftMatrixChanged(inventoryIn);
//			StorageShellEntity.this.onCraftMatrixChanged();
//		}
//	}, 3, 3);
//	public IInventory craftResult = new InventoryCraftResult();
//
//	public CraftingContainer getCraftMatrix() {
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
//	protected void onSlotChanged(Slot slot) {
//		super.onSlotChanged(slot);
//		if (slot.index >= 0 && slot.index < 9)
//			this.craftMatrix.setItem(slot.index, slot.getItem());
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
