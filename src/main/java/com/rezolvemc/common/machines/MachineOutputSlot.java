package com.rezolvemc.common.machines;

import org.torchmc.inventory.OutputSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MachineOutputSlot extends OutputSlot {
	public MachineOutputSlot(Container inventoryIn, int index) {
		super(inventoryIn, index);
	}

	@Override
	public void onTake(Player pPlayer, ItemStack pStack) {
		if (this.container instanceof IMachineInventory)
			((IMachineInventory)this.container).outputSlotActivated(this.getSlotIndex());

		super.onTake(pPlayer, pStack);
	}
}