package com.astronautlabs.mc.rezolve.common;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MachineOutputSlot extends OutputSlot {
	public MachineOutputSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public void onTake(Player pPlayer, ItemStack pStack) {
		if (this.container instanceof IMachineInventory)
			((IMachineInventory)this.container).outputSlotActivated(this.getSlotIndex());

		super.onTake(pPlayer, pStack);
	}
}