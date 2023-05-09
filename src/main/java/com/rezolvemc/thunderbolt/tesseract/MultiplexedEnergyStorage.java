package com.rezolvemc.thunderbolt.tesseract;

import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class MultiplexedEnergyStorage implements IEnergyStorage {
    @NotNull
    public abstract List<IEnergyStorage> getHandlers();

    public boolean isEmpty() {
        return getHandlers().isEmpty();
    }

    private int receiveCounter = 0;
    private int extractCounter = 0;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = 0;

        var handlers = getHandlers();
        for (int i = 0, max = handlers.size(); i < max; ++i) {
            var handler = handlers.get((receiveCounter + i) % max);
            accepted = handler.receiveEnergy(maxReceive, simulate);
            maxReceive -= accepted;
            if (maxReceive <= 0)
                break;
        }

        if (!simulate)
            receiveCounter += 1;

        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = 0;

        var handlers = getHandlers();
        for (int i = 0, max = handlers.size(); i < max; ++i) {
            var handler = handlers.get((extractCounter + i) % max);
            extracted = handler.extractEnergy(maxExtract, simulate);
            maxExtract -= extracted;
            if (maxExtract <= 0)
                break;
        }

        if (!simulate)
            extractCounter += 1;

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
