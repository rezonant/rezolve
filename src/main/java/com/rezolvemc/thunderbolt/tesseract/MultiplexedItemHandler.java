package com.rezolvemc.thunderbolt.tesseract;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiplexedItemHandler implements IItemHandler {
    @NotNull public abstract List<IItemHandler> getHandlers();

    public boolean isEmpty() {
        return getHandlers().isEmpty();
    }

    @Override
    public int getSlots() {
        int count = 0;
        for (var handler : getHandlers())
            count += handler.getSlots();
        return count;
    }

    private record MappedSlot(IItemHandler handler, int slot) {
    }

    private MappedSlot mapSlot(int slot) {
        int bottomSlot = 0;
        for (var handler : getHandlers()) {
            if (slot < bottomSlot + handler.getSlots()) {
                return new MappedSlot(handler, slot - bottomSlot);
            }
            bottomSlot += handler.getSlots();
        }

        return null;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        var mappedSlot = mapSlot(slot);
        if (mappedSlot != null)
            return mappedSlot.handler.getStackInSlot(mappedSlot.slot);
        else
            return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        var mappedSlot = mapSlot(slot);
        if (mappedSlot != null)
            return mappedSlot.handler.insertItem(mappedSlot.slot, stack, simulate);
        else
            return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        var mappedSlot = mapSlot(slot);
        if (mappedSlot != null)
            return mappedSlot.handler.extractItem(mappedSlot.slot, amount, simulate);
        else
            return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        var mappedSlot = mapSlot(slot);
        if (mappedSlot != null)
            return mappedSlot.handler.getSlotLimit(mappedSlot.slot);
        else
            return 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        var mappedSlot = mapSlot(slot);
        if (mappedSlot != null)
            return mappedSlot.handler.isItemValid(mappedSlot.slot, stack);
        else
            return false;
    }
}
