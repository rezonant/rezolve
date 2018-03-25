package com.astronautlabs.mc.rezolve.network.cable;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.api.energy.IEnergyTransport;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class CableEntity extends TileEntityBase implements IEnergyTransport {

	public CableEntity() {
		super("cable_tile_entity");

	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return (T) new SidedEnergyProxy(this, facing);
		}
		return super.getCapability(capability, facing);
	}
	@Override
	public int getEnergyStored(EnumFacing from) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return 0;
	}

	@Override
	public InterfaceType getTransportState(EnumFacing from) {
		return InterfaceType.BALANCE;
	}

	@Override
	public boolean setTransportState(InterfaceType state, EnumFacing from) {
		return false;
	}

	public CableNetwork getNetwork() {
		return CableNetwork.networkAt(this.worldObj, this.getPos(), RezolveMod.ETHERNET_CABLE_BLOCK);
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
		BlockPos sender = this.getPos().offset(from);

		int energy = 0;

		for (BlockPos endpointPos : this.getNetwork().getEndpointBlocks()) {
			if (endpointPos.equals(sender))
				continue;

			TileEntity entity = this.worldObj.getTileEntity(endpointPos);
			int extracted = 0;

			if (entity instanceof IEnergyProvider) {
				extracted = ((IEnergyProvider)entity).extractEnergy(from, maxExtract, simulate);
			} else if (entity.hasCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN)) {
				// TODO: not only DOWN
				IEnergyStorage storage = entity.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN);
				extracted = storage.extractEnergy(maxExtract, simulate);
			}

			energy += extracted;
			maxExtract -= extracted;

			if (maxExtract <= 0)
				break;
		}

		return energy;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		BlockPos sender = this.getPos().offset(from);

		int energy = 0;

		for (CableNetwork.Endpoint endpoint : this.getNetwork().getEndpoints()) {
			BlockPos endpointPos = endpoint.getPosition();

			if (endpointPos.equals(sender))
				continue;

			TileEntity entity = this.worldObj.getTileEntity(endpointPos);
			int received = 0;

			if (entity == null)
				continue;

			if (entity instanceof IEnergyReceiver) {
				// RF
				received = ((IEnergyReceiver)entity).receiveEnergy(from, maxReceive, simulate);
			} else {
				// Forge
				for (EnumFacing side : endpoint.getFacing()) {
					if (entity.hasCapability(CapabilityEnergy.ENERGY, side)) {
						IEnergyStorage storage = entity.getCapability(CapabilityEnergy.ENERGY, side);
						received = storage.receiveEnergy(maxReceive, simulate);
					}
				}
			}

			energy += received;
			maxReceive -= received;

			if (maxReceive <= 0)
				break;
		}

		return energy;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}
}
