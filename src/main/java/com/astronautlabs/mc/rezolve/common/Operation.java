package com.astronautlabs.mc.rezolve.common;

import net.minecraft.nbt.NBTTagCompound;

public abstract class Operation<T extends MachineEntity> {
	public Operation(T machine) {
		this.machine = machine;
	}
	
	private T machine;
	
	public T getMachine() {
		return this.machine;
	}
	
	public abstract void writeNBT(NBTTagCompound nbt);
	public abstract void readNBT(NBTTagCompound nbt);
	public abstract int getPercentage();
	
	/**
	 * Process an update on this operation. 
	 * @return True if the operation is completed, false if the operation continues.
	 */
	public abstract boolean update();
}
