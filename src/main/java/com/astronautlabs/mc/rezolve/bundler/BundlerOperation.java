package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity.ItemMemo;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.common.Operation;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BundlerOperation extends Operation<BundlerEntity> {

	public BundlerOperation(BundlerEntity machine) {
		super(machine);
	}
	
	public BundlerOperation(BundlerEntity machine, ItemStack pattern) {
		super(machine);
		this.pattern = pattern;
		this.energyStored = 0;
		this.energyRequired = BundleItem.getBundleCost(pattern);
		this.timeStarted = machine.getWorld().getTotalWorldTime();
		this.timeRequired = 3 * 20;
	}

	private int energyStored;
	private int energyRequired;
	private ItemStack pattern;
	private long timeRequired = 0;
	private long timeStarted = 0;
	
	@Override
	public void writeNBT(NBTTagCompound nbt) {
		nbt.setInteger("Op_Energy", this.energyStored);
		nbt.setInteger("Op_EnergyRequired", this.energyRequired);
		nbt.setLong("Op_TimeRequired", this.timeRequired);
		nbt.setLong("Op_TimeStarted", this.timeStarted);
		
		if (this.pattern != null) {
			NBTTagCompound stack = new NBTTagCompound();
			this.pattern.writeToNBT(nbt);		
			nbt.setTag("Op_Stack", stack);
		}
		
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		energyStored = nbt.getInteger("Op_Energy");
		energyRequired = nbt.getInteger("Op_EnergyRequired");
		timeRequired = nbt.getInteger("Op_TimeRequired");
		timeStarted = nbt.getInteger("Op_TimeStarted");
		
		if (nbt.hasKey("Op_Stack"))
			pattern = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Op_Stack"));
	}

	@Override
	public int getPercentage() {
		// TODO Auto-generated method stub
		
		long timeSinceStart = this.getMachine().getWorld().getTotalWorldTime() - this.timeStarted;
		
		int timePercentage = (int)(timeSinceStart / (double)this.timeRequired * 100);
		int energyPercentage = (int)(this.energyStored / (double)this.energyRequired * 100);
		return Math.min(100, Math.min(timePercentage, energyPercentage));
	}

	private boolean stillValid() {
		if (!this.getMachine().hasPattern(this.pattern))
			return false;
		
		if (!this.getMachine().hasItemsFor(this.pattern))
			return false;
		
		return true;
	}
	
	public boolean timeSatisfied() {
		long timeSinceStart = this.getMachine().getWorld().getTotalWorldTime() - this.timeStarted;
		return timeSinceStart >= this.timeRequired;
	}
	
	@Override
	public boolean update() {
		
		// If we aren't valid anymore, return true (operation finished)
		if (!this.stillValid())
			return true;
		
		this.energyStored += this.getMachine().takeEnergy(this.energyRequired - this.energyStored);
		
		if (this.energyStored < this.energyRequired) 
			return false;

		if (!this.timeSatisfied())
			return false;
		
		// If there's no space, we'll just hold the operation up until there is space...
		
		if (!this.getMachine().storeBundle(this.getMachine().makeBundleStack(this.pattern), true))
			return false;
		
		// Do the thing

		this.getMachine().completeOperation(this);
		return true;
	}

	public ItemStack getPattern() {
		return this.pattern;
	}

	public int getStoredEnergy() {
		return this.energyStored;
	}
	
	
	
}
