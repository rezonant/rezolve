package com.rezolvemc.thunderbolt.tesseract;

import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MultiplexedEnergyStorage implements IEnergyStorage {
    @NotNull
    public abstract List<IEnergyStorage> getHandlers();

    public boolean isEmpty() {
        return getHandlers().isEmpty();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = 0;
        for (var handler : getHandlers()) {
            accepted = handler.receiveEnergy(maxReceive, simulate);
            maxReceive -= accepted;
            if (maxReceive <= 0)
                break;
        }

        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = 0;
        for (var handler : getHandlers()) {
            extracted = handler.extractEnergy(maxExtract, simulate);
            maxExtract -= extracted;
            if (maxExtract <= 0)
                break;
        }

        return extracted;
    }

    @Override
    public int getEnergyStored() {
        int stored = 0;

        for (var handler : getHandlers())
            stored += handler.getEnergyStored();

        return stored;
    }

    @Override
    public int getMaxEnergyStored() {
        int maxEnergyStored = 0;

        for (var handler : getHandlers())
            maxEnergyStored += handler.getMaxEnergyStored();

        return maxEnergyStored;
    }

    @Override
    public boolean canExtract() {
        for (var handler : getHandlers())
            if (handler.canExtract())
                return true;
        return false;
    }

    @Override
    public boolean canReceive() {
        for (var handler : getHandlers())
            if (handler.canReceive())
                return true;
        return false;
    }
}
