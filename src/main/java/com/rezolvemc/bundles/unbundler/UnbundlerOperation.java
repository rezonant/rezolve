package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.bundles.BundleItem;
import com.rezolvemc.common.machines.Operation;

import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@RegistryId("unbundler")
public class UnbundlerOperation extends Operation<UnbundlerEntity> {

	public UnbundlerOperation() {
		super();
	}

	public UnbundlerOperation(UnbundlerEntity machine) {
		super(machine);
	}
	
	public UnbundlerOperation(UnbundlerEntity machine, ItemStack bundle) {
		super(machine);
		
		this.bundle = bundle;
		this.timeStarted = machine.getLevel().getGameTime();
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
	protected CompoundTag writeNBT() {
		var nbt = new CompoundTag();
		nbt.putInt("Op_EnergyRequired", this.energyRequired);
		nbt.putInt("Op_EnergyStored", this.energyStored);
		nbt.putLong("Op_TimeStarted", this.timeStarted);
		nbt.putLong("Op_TimeRequired", this.timeRequired);

		if (this.bundle != null) {
			CompoundTag stack = this.bundle.serializeNBT();
			nbt.put("Op_Stack", stack);
		}

		return nbt;
	}
	
	@Override
	protected void readNBT(CompoundTag nbt) {
		this.energyRequired = nbt.getInt("Op_EnergyRequired");
		this.energyStored = nbt.getInt("Op_EnergyStored");
		this.timeStarted = nbt.getLong("Op_TimeStarted");
		this.timeRequired = nbt.getLong("Op_TimeRequired");

		if (nbt.contains("Op_Stack"))
			this.bundle = ItemStack.of(nbt.getCompound("Op_Stack"));
	}

	@Override
	public float computeProgress() {
		long timeSinceStart = this.getMachine().getLevel().getGameTime() - this.timeStarted;
		float timePercentage = timeSinceStart / (float)this.timeRequired;
		float energyPercentage = this.energyStored / (float)this.energyRequired;
		return Math.min(1.0f, Math.min(timePercentage, energyPercentage));
	}
	
	private boolean stillValid() {
		if (!this.getMachine().hasBundle(this.bundle))
			return false;
		
		return true;
	}

	public boolean timeSatisfied() {
		long timeSinceStart = this.getMachine().getLevel().getGameTime() - this.timeStarted;
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
