package com.rezolvemc.thunderbolt.tesseract;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.torchmc.util.Values;

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

    int fillCounter = 0;
    int drainCounter = 0;

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int filled = 0;

        resource = resource.copy();

        var handlers = getHandlers();
        for (int i = 0, max = handlers.size(); i < max; ++i) {
            var handler = handlers.get((fillCounter + i) % max);
            var newlyFilled = handler.fill(resource, action);
            filled += newlyFilled;
            resource.shrink(newlyFilled);
            if (resource.isEmpty())
                break;
        }

        if (action == FluidAction.EXECUTE)
            fillCounter += 1;

        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = null;

        var handlers = getHandlers();
        for (int i = 0, max = handlers.size(); i < max; ++i) {
            var handler = handlers.get((drainCounter + i) % max);
            var newlyDrained = handler.drain(resource, action);
            if (newlyDrained.isEmpty())
                continue;

            if (drained == null)
                drained = newlyDrained.copy();
            else if (drained.isFluidEqual(newlyDrained))
                drained.grow(newlyDrained.getAmount());

            resource.shrink(newlyDrained.getAmount());
        }

        if (action == FluidAction.EXECUTE)
            drainCounter += 1;

        return Values.coalesce(drained, FluidStack.EMPTY);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = null;

        var handlers = getHandlers();
        for (int i = 0, max = handlers.size(); i < max; ++i) {
            var handler = handlers.get((drainCounter + i) % max);
            var newlyDrained = handler.drain(maxDrain, action);
            if (newlyDrained.isEmpty())
                continue;

            if (drained == null)
                drained = newlyDrained.copy();
            else if (drained.isFluidEqual(newlyDrained))
                drained.grow(newlyDrained.getAmount());

            maxDrain -= newlyDrained.getAmount();
        }

        if (action == FluidAction.EXECUTE)
            drainCounter += 1;

        return Values.coalesce(drained, FluidStack.EMPTY);
    }
}
