package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MachineEntity extends TileEntityBase implements IInventory, ITickable, IEnergyReceiver, IMachineInventory, ICapabilityProvider, IItemHandler {
	public MachineEntity(String registryName) {
		super(registryName);
		this.inventory = new ItemStack[this.getSizeInventory()];
		this.storedEnergy = 0;
	}
	
    protected ItemStack[] inventory;

    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }
    
	@Override
	public String getName() {
	    return this.hasCustomName() ? this.getCustomName() : "container."+this.getRegistryName();
	}

	@Override
	public boolean hasCustomName() {
	    return this.getCustomName() != null && !"".equals(this.getCustomName());
	}

	@Override
	public int getSizeInventory() {
		return 0;
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

	public boolean allowInputToSlot(int index) {
		return true;
	}
	
	public boolean allowOutputFromSlot(int index) {
		return true;
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
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	    for (int i = 0; i < this.getSizeInventory(); i++)
	        this.setInventorySlotContents(i, null);
	}

    private long lastUpdate = 0;
    
    /**
     * Fire an update every N ticks (20 ticks per second)
     */
    protected long updateInterval = 20 * 2;
    
    /**
     * This is run every `updateInterval` with the default implementation
     * of update()
     */
    public void updatePeriodically() {
    		// TODO: implement in subclass
    }
    
	@Override
	public void update() {
		
		if (this.worldObj.isRemote)
			return;
		
		long currentTime = this.worldObj.getTotalWorldTime();
		
		if (lastUpdate + updateInterval > currentTime)
			return;
		
		lastUpdate = currentTime;
		this.updatePeriodically();
		
	}

	protected int maxEnergyStored = 20000;
	
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

	@Override
	public void outputSlotActivated(int index) {
		
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) this;
		}
		
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		
		return super.hasCapability(capability, facing);
	}

	@Override
	public int getSlots() {
		return this.getSizeInventory();
	}

	protected boolean allowedToPullFrom(int slot) {
		return true;
	}
	
	protected boolean allowedToPushTo(int slot) {
		return true;
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		System.out.println("Trying to push into "+this.getRegistryName() + " slot "+slot);
		if (!this.allowedToPushTo(slot))
			return null;

		System.out.println("Acceptable to push into "+this.getRegistryName()+ " slot "+slot);
		ItemStack existingStack = this.getStackInSlot(slot);
		
		if (existingStack != null && !RezolveMod.areStacksSame(stack, existingStack)) {

			System.out.println("There's a different stack in this slot: trying to push into "+this.getRegistryName()+ " slot "+slot);
			return stack;
		}
		
		System.out.println("No existing stack in this slot, should be able to push into "+this.getRegistryName()+ " slot "+slot);
		
		int itemsToKeep = stack.stackSize;
		ItemStack returnStack = null;
		ItemStack keepStack = stack.copy();
		
		if (existingStack != null && existingStack.stackSize + stack.stackSize > this.getInventoryStackLimit()) {
			itemsToKeep = this.getInventoryStackLimit() - existingStack.stackSize;
			returnStack = keepStack.splitStack(stack.stackSize - itemsToKeep);
		}

		if (existingStack != null)
			keepStack.stackSize += existingStack.stackSize;
		
		if (!simulate) {
			this.setInventorySlotContents(slot, keepStack);
		}

		System.out.println("Allowing push into "+this.getRegistryName()+ " slot "+slot);
		
		return returnStack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (!this.allowedToPullFrom(slot))
			return null;
		
		ItemStack existingStack = this.getStackInSlot(slot);
		
		if (existingStack == null)
			return null;
		
		ItemStack keepStack = null;
		ItemStack returnStack = existingStack.copy();
		
		if (existingStack.stackSize > amount) {
			returnStack = existingStack.splitStack(amount);
			keepStack = existingStack;
		}
		
		if (!simulate) {
			this.setInventorySlotContents(slot, keepStack);
		}
		
		return existingStack;
	}
}
