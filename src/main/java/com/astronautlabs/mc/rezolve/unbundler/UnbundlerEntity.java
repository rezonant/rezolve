package com.astronautlabs.mc.rezolve.unbundler;

import com.astronautlabs.mc.rezolve.common.MachineEntity;
import net.minecraft.item.ItemStack;

public class UnbundlerEntity extends MachineEntity {
	public UnbundlerEntity() {
		super("unbundler_tile_entity");
		
		this.updateInterval = 20 * 2;
		this.maxEnergyStored = 20000;
	}
	
    private int unbundleEnergyCost = 1000;

	@Override
	public int getSizeInventory() {
		return 30;
	}

	public boolean isOutputSlot(int index) {
		return index >= 26 && index <= 38;
	}
	
	public boolean isPatternSlot(int index) {
		return index >= 13 && index <= 25;
	}
	
	public boolean isInputSlot(int index) {
		return index < 13;
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (this.isInputSlot(index))
			return true;
	
		if (this.isOutputSlot(index))
			return false; 
	
		if (this.isPatternSlot(index))
			return false; // TODO: we could probably allow this
		
		return true;
	}

	@Override
	public void updatePeriodically() {
		// TODO
	}
}
