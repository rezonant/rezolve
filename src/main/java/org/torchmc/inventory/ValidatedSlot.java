package org.torchmc.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Pass an arbitrary validation function, the slot will only allow a stack if the validation function succeeds
 */
public class ValidatedSlot extends BaseSlot {
	public ValidatedSlot(Container container, int index, Validator validator) {
		super(container, index);
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
