package com.astronautlabs.mc.rezolve.common;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MachineOperation<T extends MachineEntity> extends Operation<T> {

	public MachineOperation(T machine, Delegate delegate) {
		super(machine);

		this.timeStarted = machine.getWorld().getTotalWorldTime();
		this.delegate = delegate;
	}

	protected int energyStored = 0;
	protected int energyRequired = 0;
	protected long timeRequired = 0;
	protected long timeStarted;
	protected Delegate<T> delegate = null;
	protected Validator<T> validator;
	protected VirtualInventory inventory = null;

	public static <T extends MachineEntity> Builder<T> create(T machine) {
		return new Builder<T>(machine);
	}

	public VirtualInventory getInventory() {
		return this.inventory;
	}

	public ItemStack getStackInSlot(int slot) {
		return this.inventory.getStackInSlot(slot);
	}

	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.inventory.setInventorySlotContents(slot, stack);
	}

	public interface Delegate<T extends MachineEntity> {
		boolean complete(MachineOperation<T> operation);
	}

	public interface Validator<T extends MachineEntity> {
		boolean valid(MachineOperation<T> operation);
	}

	public static class Builder<T extends MachineEntity> {
		public Builder(T machine) {
			this.machine = machine;
		}

		private T machine;
		private int energyRequired = 0;
		private int timeRequired = 0;
		private Delegate<T> delegate = null;
		private Validator<T> validator = null;

		private VirtualInventory inventory = new VirtualInventory();

		public Builder<T> requiresEnergy(int amount) {
			this.energyRequired += amount;
			return this;
		}

		public Builder<T> requiresTick(int ticks) {
			this.timeRequired += ticks;
			return this;
		}

		public Builder<T> requiresSeconds(int seconds) {
			this.timeRequired += seconds * 20;
			return this;
		}

		public Builder<T> withItems(int slot, ItemStack items) {
			this.inventory.setInventorySlotContents(slot, items);
			return this;
		}

		public Builder<T> completesWith(Delegate<T> delegate) {
			this.delegate = delegate;
			return this;
		}

		public Builder<T> validWhile(Validator<T> validator) {
			this.validator = validator;
			return this;
		}

		public MachineOperation<T> build() {
			MachineOperation<T> op = new MachineOperation<T>(this.machine, this.delegate);
			op.energyRequired = this.energyRequired;
			op.timeRequired = this.timeRequired;
			op.inventory = this.inventory;
			op.validator = this.validator;

			if (op.delegate == null)
				throw new RuntimeException("Cannot construct an operation without a completion delegate (see completesWith())");

			return op;
		}
	}

	public int getStoredEnergy() {
		return this.energyStored;
	}

	@Override
	public void writeNBT(NBTTagCompound nbt) {
		nbt.setInteger("Op_Energy", this.energyStored);
		nbt.setInteger("Op_EnergyRequired", this.energyRequired);
		nbt.setLong("Op_TimeRequired", this.timeRequired);
		nbt.setLong("Op_TimeStarted", this.timeStarted);
		RezolveNBT.writeInventory(nbt, this.inventory);
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		energyStored = nbt.getInteger("Op_Energy");
		energyRequired = nbt.getInteger("Op_EnergyRequired");
		timeRequired = nbt.getInteger("Op_TimeRequired");
		timeStarted = nbt.getInteger("Op_TimeStarted");
		RezolveNBT.readInventory(nbt, this.inventory);
	}

	@Override
	public int getPercentage() {
		long timeSinceStart = this.getMachine().getWorld().getTotalWorldTime() - this.timeStarted;

		int timePercentage = (int)(timeSinceStart / (double)this.timeRequired * 100);
		int energyPercentage = (int)(this.energyStored / (double)this.energyRequired * 100);
		return Math.min(100, Math.min(timePercentage, energyPercentage));
	}

	public boolean isTimeSatisfied() {
		long timeSinceStart = this.getMachine().getWorld().getTotalWorldTime() - this.timeStarted;
		return timeSinceStart >= this.timeRequired;
	}

	private boolean stillValid() {
		if (this.validator != null)
			return this.validator.valid(this);
		return true;
	}

	@Override
	public boolean update() {

		// If we aren't valid anymore, return true (operation finished)
		if (!this.stillValid())
			return true;

		this.energyStored += this.getMachine().takeEnergy(this.energyRequired - this.energyStored);

		if (this.energyStored < this.energyRequired)
			return false;

		if (!this.isTimeSatisfied())
			return false;

		if (this.delegate == null)
			return false;

		return this.delegate.complete(this);
	}
}
