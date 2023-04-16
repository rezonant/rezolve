package com.astronautlabs.mc.rezolve.common.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Pass an arbitrary validation function, the slot will only allow a stack if the validation function succeeds
 */
public class ValidatedSlot extends Slot {
	public ValidatedSlot(Container container, int index, int xPosition, int yPosition, Validator validator) {
		super(container, index, xPosition, yPosition);
		this.validator = validator;
	}

	public interface Validator {
		boolean validate(ItemStack stack);
	}

	private Validator validator;

	@Override
	public boolean mayPlace(ItemStack stack) {
		return this.validator.validate(stack);
	}
}
