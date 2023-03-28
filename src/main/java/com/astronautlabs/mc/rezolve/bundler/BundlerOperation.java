package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity.ItemMemo;
import com.astronautlabs.mc.rezolve.common.MachineEntity;
import com.astronautlabs.mc.rezolve.common.Operation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class BundlerOperation extends Operation<BundlerEntity> {

	public BundlerOperation(BundlerEntity machine) {
		super(machine);
	}
	
	public BundlerOperation(BundlerEntity machine, ItemStack pattern) {
		super(machine);
		this.pattern = pattern;
		this.energyStored = 0;
		this.energyRequired = BundleItem.getBundleCost(pattern);
		this.timeStarted = machine.getLevel().getGameTime();
		this.timeRequired = 3 * 20;
	}

	private int energyStored;
	private int energyRequired;
	private ItemStack pattern;
	private long timeRequired = 0;
	private long timeStarted = 0;
	
	@Override
	public void writeNBT(CompoundTag nbt) {
		nbt.putInt("Op_Energy", this.energyStored);
		nbt.putInt("Op_EnergyRequired", this.energyRequired);
		nbt.putLong("Op_TimeRequired", this.timeRequired);
		nbt.putLong("Op_TimeStarted", this.timeStarted);
		
		if (this.pattern != null) {
			CompoundTag stack = this.pattern.serializeNBT();
			nbt.put("Op_Stack", stack);
		}
		
	}

	@Override
	public void readNBT(CompoundTag nbt) {
		energyStored = nbt.getInt("Op_Energy");
		energyRequired = nbt.getInt("Op_EnergyRequired");
		timeRequired = nbt.getInt("Op_TimeRequired");
		timeStarted = nbt.getInt("Op_TimeStarted");
		
		if (nbt.contains("Op_Stack"))
			pattern = ItemStack.of(nbt.getCompound("Op_Stack"));
	}

	@Override
	public int getPercentage() {
		// TODO Auto-generated method stub
		
		long timeSinceStart = this.getMachine().getLevel().getGameTime() - this.timeStarted;
		
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
		long timeSinceStart = this.getMachine().getLevel().getGameTime() - this.timeStarted;
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
