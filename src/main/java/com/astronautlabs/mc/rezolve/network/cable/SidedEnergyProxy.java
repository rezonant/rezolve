package com.astronautlabs.mc.rezolve.network.cable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;

public class SidedEnergyProxy implements IEnergyStorage {
	public SidedEnergyProxy(CableEntity cableEntity, EnumFacing facing) {
		this.cableEntity = cableEntity;
		this.facing = facing;
	}

	private CableEntity cableEntity;
	private EnumFacing facing;

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return this.cableEntity.receiveEnergy(this.facing, maxReceive, simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return this.cableEntity.extractEnergy(this.facing, maxExtract, simulate);
	}

	@Override
	public int getEnergyStored() {
		return this.cableEntity.getEnergyStored(this.facing);
	}

	@Override
	public int getMaxEnergyStored() {
		return this.cableEntity.getMaxEnergyStored(this.facing);
	}

	@Override
	public boolean canExtract() {
		return true;
	}

	@Override
	public boolean canReceive() {
		return true;
	}
}
