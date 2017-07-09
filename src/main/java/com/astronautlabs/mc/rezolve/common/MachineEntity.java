package com.astronautlabs.mc.rezolve.common;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class MachineEntity extends TileEntityBase implements IInventory, ITickable, IEnergyReceiver, IMachineInventory {
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
}
