package com.astronautlabs.mc.rezolve.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * Adapter for IItemHandler to IStorageAccessor
 */
public class ItemHandlerStorageAccessor implements IStorageAccessor {
    public ItemHandlerStorageAccessor(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    IItemHandler itemHandler;

    @Override
    public void readItems(List<ItemStack> list) {
        for (int i = 0, max = itemHandler.getSlots(); i < max; ++i) {
            var stack = itemHandler.getStackInSlot(i);
            if (stack == null || stack.isEmpty())
                continue;

            list.add(stack);
        }
    }

    @Override
    public ItemStack giveItemStack(ItemStack stack, String hashLocator, boolean simulate) {
        stack = stack.copy();

        for (int i = 0, max = itemHandler.getSlots(); i < max && !stack.isEmpty(); ++i) {
            stack = itemHandler.insertItem(i, stack, simulate);
        }

        return stack;
    }

    @Override
    public ItemStack takeItemStack(ItemStack stack, String hashLocator, boolean simulate) {
        int remainingAmount = stack.getCount();
        int foundAmount = 0;

        for (int i = 0, max = itemHandler.getSlots(); i < max; ++i) {
            var existingStack = itemHandler.getStackInSlot(i);
            if (ItemStack.isSame(stack, existingStack)) {
                var takenStack = itemHandler.extractItem(i, remainingAmount, simulate);
                remainingAmount -= takenStack.getCount();
                foundAmount += takenStack.getCount();
            }
        }

        var finalStack = new ItemStack(stack.getItem(), foundAmount);
        finalStack.setTag(stack.getTag().copy());

        return finalStack;
    }

    @Override
    public void clear() {
        // No op, not sure if we should ever do this
    }

    @Override
    public int count(ItemStack stack, String hashLocator) {
        int amount = 0;

        for (int i = 0, max = itemHandler.getSlots(); i < max; ++i) {
            var existingStack = itemHandler.getStackInSlot(i);

            if (existingStack == null)
                continue;

            if (ItemStack.isSame(stack, existingStack))
                amount += existingStack.getCount();
        }

        return amount;
    }

    @Override
    public int getTotalItems() {
        int amount = 0;

        for (int i = 0, max = itemHandler.getSlots(); i < max; ++i) {
            var existingStack = itemHandler.getStackInSlot(i);

            if (existingStack == null)
                continue;

            amount += existingStack.getCount();
        }

        return amount;
    }

    @Override
    public int getTotalStacks() {
        int amount = 0;

        for (int i = 0, max = itemHandler.getSlots(); i < max; ++i) {
            var existingStack = itemHandler.getStackInSlot(i);

            if (existingStack == null || existingStack.isEmpty())
                continue;

            amount += 1;
        }

        return amount;
    }

    @Override
    public int getSize() {

        int amount = 0;

        for (int i = 0, max = itemHandler.getSlots(); i < max; ++i) {
            amount += itemHandler.getSlotLimit(i);
        }

        return amount;
    }
}
