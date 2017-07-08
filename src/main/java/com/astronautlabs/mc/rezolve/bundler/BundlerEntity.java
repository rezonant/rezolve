package com.astronautlabs.mc.rezolve.bundler;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.BundlerNBT;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BundlerEntity extends TileEntityBase implements IInventory, ITickable, IEnergyReceiver {
	public BundlerEntity() {
		this.inventory = new ItemStack[this.getSizeInventory()];
		this.storedEnergy = 0;
	}
	
	public static void register() {
		GameRegistry.registerTileEntity(BundlerEntity.class, "bundler_tile_entity");
	}

    private ItemStack[] inventory;
    private int bundleEnergyCost = 100;
    
    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }
    
	@Override
	public String getName() {
	    return this.hasCustomName() ? this.getCustomName() : "container.bundler_tile_entity";
	}

	@Override
	public boolean hasCustomName() {
	    return this.getCustomName() != null && !"".equals(this.getCustomName());
	}

	@Override
	public int getSizeInventory() {
		return 30;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= this.getSizeInventory())
	        return null;
	    return this.inventory[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (this.getStackInSlot(index) != null) {
	        ItemStack itemstack;

	        if (this.getStackInSlot(index).stackSize <= count) {
	            itemstack = this.getStackInSlot(index);
	            this.setInventorySlotContents(index, null);
	            this.markDirty();
	            return itemstack;
	        } else {
	            itemstack = this.getStackInSlot(index).splitStack(count);

	            if (this.getStackInSlot(index).stackSize <= 0) {
	                this.setInventorySlotContents(index, null);
	            } else {
	                //Just to show that changes happened
	                this.setInventorySlotContents(index, this.getStackInSlot(index));
	            }

	            this.markDirty();
	            return itemstack;
	        }
	    } else {
	        return null;
	    }
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
	    if (index < 0 || index >= this.getSizeInventory())
	        return;

	    if (stack != null && stack.stackSize > this.getInventoryStackLimit())
	        stack.stackSize = this.getInventoryStackLimit();
	        
	    if (stack != null && stack.stackSize == 0)
	        stack = null;

	    this.inventory[index] = stack;
	    this.markDirty();	
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
	    return this.worldObj.getTileEntity(this.getPos()) == this && player.getDistanceSq(this.pos.add(0.5, 0.5, 0.5)) <= 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	public boolean isOutputSlot(int index) {
		return index >= 26 && index <= 38;
	}
	
	public boolean isPatternSlot(int index) {
		return index >= 13 && index <= 25;
	}
	
	public boolean isInputSlot(int index) {
		return index < 13;
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

	@Override
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
	    for (int i = 0; i < this.getSizeInventory(); i++)
	        this.setInventorySlotContents(i, null);
	}
    
	private ItemStack createItemFromPattern(ItemStack pattern) {
		
		this.dummyInventory.clear();
		
		if (!pattern.hasTagCompound())
			return null;
		
		NBTTagCompound nbt = pattern.getTagCompound();
		BundlerNBT.readInventory(nbt, this.dummyInventory);
		
		ArrayList<ItemStack> collectedItems = new ArrayList<ItemStack>();
		
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
		ItemStack bundleStack = new ItemStack(RezolveMod.instance().bundleItem, 1);
		NBTTagCompound bundleNbt = new NBTTagCompound();
		nbt.setTag("Items", nbt.getTag("Items"));
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
    
    private long lastUpdate = 0;
    private long updateInterval = 20 * 2;
    
	@Override
	public void update() {
		
		if (this.worldObj.isRemote)
			return;
		
		long currentTime = this.worldObj.getTotalWorldTime();
		
		if (lastUpdate + updateInterval > currentTime)
			return;
		
		lastUpdate = currentTime;
		this.produceBundles();
		
	}

	private int maxEnergyStored = 1000;
	
	@Override
	public int getEnergyStored(EnumFacing arg0) {
		// TODO Auto-generated method stub
		return this.storedEnergy;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing arg0) {
		// TODO Auto-generated method stub
		return this.maxEnergyStored;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing arg0) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		int availableStorage = this.maxEnergyStored - this.storedEnergy;
		int receivedEnergy = Math.min(availableStorage, maxReceive);
		
		if (!simulate) {
			this.storedEnergy += receivedEnergy;
			this.notifyUpdate();
		}
		
		return receivedEnergy;
	}
}
