package com.astronautlabs.mc.rezolve.storage.machines.storageShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.core.inventory.PersistedInventoryCrafting;
import com.astronautlabs.mc.rezolve.network.cable.CableNetwork;
import com.astronautlabs.mc.rezolve.storage.IItemStorage;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import com.astronautlabs.mc.rezolve.storage.IStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.IStorageTileEntity;
import com.astronautlabs.mc.rezolve.storage.NetworkStorageAccessor;
import com.astronautlabs.mc.rezolve.storage.machines.diskBay.DiskAccessor;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import java.util.ArrayList;
import java.util.List;

public class StorageShellEntity extends MachineEntity implements IStorageTileEntity {
	public StorageShellEntity() {
		super("storage_shell_tile_entity");
	}

	@Override
	public int getSizeInventory() {
		return 10;
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
	public Class<? extends MachineGui> getGuiClass() {
		return StorageShellGui.class;
	}

	@Override
	protected void updatePeriodicallyOnClient() {
		// Client-side
		super.updatePeriodicallyOnClient();
	}

	public CableNetwork getNetwork() {
		if (this.worldObj.isRemote)
			return null;

		return CableNetwork.networkAt(this.worldObj, this.pos, RezolveMod.ETHERNET_CABLE_BLOCK, true);
	}

	public NetworkStorageAccessor getNetworkStorage() {
		CableNetwork network = this.getNetwork();

		if (network != null)
			return network.getNetworkStorage();

		return null;
	}


	private InventoryCrafting craftMatrix = new PersistedInventoryCrafting(this, 0, new Container() {
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}

		@Override
		public void onCraftMatrixChanged(IInventory inventoryIn) {
			super.onCraftMatrixChanged(inventoryIn);
			StorageShellEntity.this.onCraftMatrixChanged();
		}
	}, 3, 3);
	public IInventory craftResult = new InventoryCraftResult();

	public InventoryCrafting getCraftMatrix() {
		return this.craftMatrix;
	}

	public IInventory getCraftResult() {
		return this.craftResult;
	}

	public void onCraftMatrixChanged() {
		ItemStack matchingRecipe = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj);
		this.craftResult.setInventorySlotContents(0, matchingRecipe);
	}

	@Override
	public void onSlotChanged(int index, ItemStack stack) {
		super.onSlotChanged(index, stack);
		if (index >= 0 && index < 9)
			this.craftMatrix.setInventorySlotContents(index, stack);
	}

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

			if (remainingStack != null && remainingStack.stackSize <= 0)
				remainingStack = null;

			this.setInventorySlotContents(i, remainingStack);

			if (remainingStack != null) {
				return false;
			}
		}

		return true;
	}
}
