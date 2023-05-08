package com.rezolvemc.thunderbolt.tesseract;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class MultiplexedFluidHandler implements IFluidHandler {
    public abstract List<IFluidHandler> getHandlers();

    public boolean isEmpty() {
        return getHandlers().isEmpty();
    }

    private record MappedTank(IFluidHandler handler, int tank) {
    }

    private MappedTank mapSlot(int tank) {
        int bottomTank = 0;
        for (var handler : getHandlers()) {
            if (tank < bottomTank + handler.getTanks()) {
                return new MappedTank(handler, tank - bottomTank);
            }
            bottomTank += handler.getTanks();
        }

        return null;
    }

    @Override
    public int getTanks() {
        int tanks = 0;
        for (var handler : getHandlers())
            tanks += handler.getTanks();
        return tanks;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        var mappedTank = mapSlot(tank);
        if (mappedTank != null)
            return mappedTank.handler.getFluidInTank(mappedTank.tank);

        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        var mappedTank = mapSlot(tank);
        if (mappedTank != null)
            return mappedTank.handler.getTankCapacity(mappedTank.tank);

        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        var mappedTank = mapSlot(tank);
        if (mappedTank != null)
            return mappedTank.handler.isFluidValid(mappedTank.tank, stack);

        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int filled = 0;

        resource = resource.copy();

        for (var handler : getHandlers()) {
            var newlyFilled = handler.fill(resource, action);
            filled += newlyFilled;
            resource.shrink(newlyFilled);
            if (resource.isEmpty())
                break;
        }

        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = null;

        for (var handler : getHandlers()) {
            var newlyDrained = handler.drain(resource, action);

            if (drained == null)
                drained = newlyDrained;
            else if (drained.isFluidEqual(newlyDrained))
                drained.grow(newlyDrained.getAmount());

            resource.shrink(newlyDrained.getAmount());
        }

        return drained;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = null;

        for (var handler : getHandlers()) {
            var newlyDrained = handler.drain(maxDrain, action);

            if (drained == null)
                drained = newlyDrained;
            else if (drained.isFluidEqual(newlyDrained))
                drained.grow(newlyDrained.getAmount());

            maxDrain -= newlyDrained.getAmount();
        }

        return drained;
    }
}
