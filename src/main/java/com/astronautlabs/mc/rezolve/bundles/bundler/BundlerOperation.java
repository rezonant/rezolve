package com.astronautlabs.mc.rezolve.bundles.bundler;

import com.astronautlabs.mc.rezolve.bundles.BundleItem;
import com.astronautlabs.mc.rezolve.common.machines.Operation;

import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@RegistryId("bundler")
public class BundlerOperation extends Operation<BundlerEntity> {

	public BundlerOperation() {
		super(null);
	}

	public BundlerOperation(BundlerEntity machine) {
		super(machine);
	}
	
	public BundlerOperation(BundlerEntity machine, ItemStack pattern) {
		super(machine);
		this.pattern = pattern;
		this.energyStored = 0;
		this.energyRequired = BundleItem.getBundleCost(pattern);
		this.timeStarted = machine.getLevel().getGameTime();
		this.timeRequired = 10;
	}

	private int energyStored;
	private int energyRequired;
	private ItemStack pattern;
	private long timeRequired = 0;
	private long timeStarted = 0;
	
	@Override
	public CompoundTag writeNBT() {
		var nbt = new CompoundTag();
		nbt.putInt("energyStored", this.energyStored);
		nbt.putInt("energyRequired", this.energyRequired);
		nbt.putLong("timeRequired", this.timeRequired);
		nbt.putLong("timeStarted", this.timeStarted);
		
		if (this.pattern != null) {
			CompoundTag stack = this.pattern.serializeNBT();
			nbt.put("Op_Stack", stack);
		}

		return nbt;
	}

	@Override
	protected void readNBT(CompoundTag nbt) {
		energyStored = nbt.getInt("energyStored");
		energyRequired = nbt.getInt("energyRequired");
		timeRequired = nbt.getInt("timeRequired");
		timeStarted = nbt.getInt("timeStarted");
		
		if (nbt.contains("Op_Stack"))
			pattern = ItemStack.of(nbt.getCompound("Op_Stack"));
	}

	@Override
	public float computeProgress() {
		long timeSinceStart = this.getMachine().getLevel().getGameTime() - this.timeStarted;
		float timePercentage = timeSinceStart / (float)this.timeRequired;
		float energyPercentage = this.energyStored / (float)this.energyRequired;
		return Math.min(1.0f, Math.min(timePercentage, energyPercentage));
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
