package org.torchmc.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

public class ContainerItemHandler implements IItemHandler {
    public ContainerItemHandler(Container container) {
        this.container = container;
    }

    private Container container;

    Container getContainer() {
        return this.container;
    }

    @Override
    public int getSlots() {
        return this.container.getContainerSize();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.container.getItem(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack == null)
            return ItemStack.EMPTY;

        ItemStack existingStack = this.container.getItem(slot);
        if (existingStack == null)
            existingStack = ItemStack.EMPTY;
        existingStack = existingStack.copy();
        ItemStack remainingStack = stack.copy();

        if (existingStack == null)
            existingStack = ItemStack.EMPTY.copy();

        if (existingStack.isEmpty()) {
            ItemStack insertStack = remainingStack.split(container.getMaxStackSize());
            if (!simulate)
                container.setItem(slot, insertStack);

            return remainingStack;
        }

        if (!ItemHandlerHelper.canItemStacksStack(existingStack, stack))
            return remainingStack;

        int amountToInsert = Math.min(container.getMaxStackSize() - existingStack.getCount(), stack.getCount());
        remainingStack.split(amountToInsert);
        existingStack.setCount(existingStack.getCount() + amountToInsert);

        return remainingStack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack existingStack = this.container.getItem(slot);
        if (existingStack == null)
            existingStack = ItemStack.EMPTY;
        existingStack = existingStack.copy();

        ItemStack returnedStack = existingStack.split(amount);

        if (!simulate) {
            this.container.setItem(slot, existingStack);
        }

        return returnedStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }
}
