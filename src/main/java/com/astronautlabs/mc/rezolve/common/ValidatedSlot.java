package com.astronautlabs.mc.rezolve.common;

import com.astronautlabs.mc.rezolve.RezolveMod;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Pass an arbitrary validation function, the slot will only allow a stack if the validation function succeeds
 */
public class ValidatedSlot extends Slot {
	public ValidatedSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, Validator validator) {
		super(inventoryIn, index, xPosition, yPosition);
		this.validator = validator;
	}

	public interface Validator {
		boolean validate(ItemStack stack);
	}

	private Validator validator;

	@Override
	public boolean isItemValid(ItemStack stack) {
		return this.validator.validate(stack);
	}
}
