package com.astronautlabs.mc.rezolve.bundles.machines.unbundler;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.common.Operation;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class UnbundlerOperation extends Operation<UnbundlerEntity> {

	public UnbundlerOperation(UnbundlerEntity machine) {
		super(machine);
	}
	
	public UnbundlerOperation(UnbundlerEntity machine, ItemStack bundle) {
		super(machine);
		
		this.bundle = bundle;
		this.timeStarted = machine.getWorld().getTotalWorldTime();
		this.timeRequired = 3 * 20;
		this.energyRequired = BundleItem.getBundleCost(bundle);
		this.energyStored = 0;
	}
	
	private int energyRequired;
	private int energyStored;
	private long timeStarted;
	private long timeRequired;
	private ItemStack bundle;
	
	@Override
	public void writeNBT(NBTTagCompound nbt) {
		nbt.setInteger("Op_EnergyRequired", this.energyRequired);
		nbt.setInteger("Op_EnergyStored", this.energyStored);
		nbt.setLong("Op_TimeStarted", this.timeStarted);
		nbt.setLong("Op_TimeRequired", this.timeRequired);

		if (this.bundle != null) {
			NBTTagCompound stack = new NBTTagCompound();
			this.bundle.writeToNBT(nbt);		
			nbt.setTag("Op_Stack", stack);
		}
	}
	
	@Override
	public void readNBT(NBTTagCompound nbt) {
		this.energyRequired = nbt.getInteger("Op_EnergyRequired");
		this.energyStored = nbt.getInteger("Op_EnergyStored");
		this.timeStarted = nbt.getLong("Op_TimeStarted");
		this.timeRequired = nbt.getLong("Op_TimeRequired");

		if (nbt.hasKey("Op_Stack"))
			this.bundle = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Op_Stack"));
	}

	@Override
	public int getPercentage() {
		long timeSinceStart = this.getMachine().getWorld().getTotalWorldTime() - this.timeStarted;
		int timePercentage = (int)(timeSinceStart / (double)this.timeRequired * 100);
		int energyPercentage = (int)(this.energyStored / (double)this.energyRequired * 100);
		
		return Math.min(100, Math.min(timePercentage, energyPercentage));
	}
	
	private boolean stillValid() {
		if (!this.getMachine().hasBundle(this.bundle))
			return false;
		
		return true;
	}

	public boolean timeSatisfied() {
		long timeSinceStart = this.getMachine().getWorld().getTotalWorldTime() - this.timeStarted;
		return timeSinceStart >= this.timeRequired;
	}
	
	@Override
	public boolean update() {
		if (!this.stillValid())
			return true;

		this.energyStored += this.getMachine().takeEnergy(this.energyRequired - this.energyStored);
		
		if (this.energyStored < this.energyRequired)
			return false;
		
		if (!this.timeSatisfied())
			return false;
		
		// Do work!

		if (!this.getMachine().attemptUnbundle(this.bundle))
			return false;
		
		return true;
	}
}
