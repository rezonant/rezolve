package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.RezolveNBT;
import com.astronautlabs.mc.rezolve.common.MachineEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BundleBuilderEntity extends MachineEntity {
	public BundleBuilderEntity() {
		super("bundle_builder_tile_entity");
		
		maxEnergyStored = 20000;
	}
	
    public static final int PATTERN_INPUT_SLOT = 0;
    public static final int PATTERN_OUTPUT_SLOT = 1;
    public static final int DYE_SLOT = 2;
    
	@Override
	public int getSizeInventory() {
		return 12;
	}

	public void updateOutputSlot() { 
		
		boolean clearOutputSlot = false;

		// Make sure we have at least one blank pattern
		
		if (this.hasBlankPattern()) {
			// Make sure we have at least one item in the input item slots
			if (!this.hasInputItems()) {
				clearOutputSlot = true;
			}
		} else {
			clearOutputSlot = true;
		}
		
		// Make sure we have enough power to produce a pattern 
		
		if (this.storedEnergy < this.energyCost)
			clearOutputSlot = true;
		
		if (clearOutputSlot) {
			this.setInventorySlotContents(PATTERN_OUTPUT_SLOT, null);
		} else {
			this.producePattern();
		}
		
	}
	
	private int energyCost = 100;
	
	private void producePattern() {
		
		ItemStack stack = new ItemStack(RezolveMod.bundlePatternItem, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		
		RezolveNBT.writeInventory(nbt, this, 3, 9);
		
		if (this.patternName != null)
			nbt.setString("Name", this.patternName);
		
		int dye = this.dyeValue();
		if (dye != 0)
			nbt.setInteger("Color", this.dyeValue());
		
		stack.setTagCompound(nbt);
		this.setInventorySlotContents(PATTERN_OUTPUT_SLOT, stack);
	}
	
	public int dyeValue() {
		ItemStack stack = this.getStackInSlot(DYE_SLOT);
		
		if (stack == null || stack.stackSize == 0)
			return 0;
		
		return stack.getItem().getDamage(stack);
	}
	
	public boolean hasInputItems() {
		for (int i = 3, max = this.getSizeInventory(); i < max; ++i) {
			ItemStack stack = this.getStackInSlot(i);
			if (stack != null && stack.stackSize > 0)
				return true;
		}
		
		return false;
	}
	
	public boolean hasBlankPattern() {
		ItemStack inputStack = this.getStackInSlot(PATTERN_INPUT_SLOT);
		
		if (inputStack == null)
			return false;
		
		if (inputStack.stackSize == 0)
			return false;
		
		return true;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
	    	super.setInventorySlotContents(index, stack);
	    	
	    if (index != PATTERN_OUTPUT_SLOT)
	    		this.updateOutputSlot();
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {

		if (index == PATTERN_OUTPUT_SLOT) {
			
			// Automation should never push into the pattern output slot,
			// as the only reason to put an item in there is to edit an existing 
			// pattern, which is not possible with automation.
			
			return false;

		} else if (index == PATTERN_INPUT_SLOT) {
				
			return RezolveMod.instance().isBundlePatternItem(stack.getItem());
			
		} else if (index == DYE_SLOT) {
			
			// Allow it if it's a dye
			
			return RezolveMod.instance().isDye(stack.getItem());
		}
		
		return true;
	}

	@Override
	public void outputSlotActivated(int index) {
		
		// Consume a single pattern
		
		ItemStack inputStack = this.getStackInSlot(PATTERN_INPUT_SLOT);
		if (inputStack == null) {
			return;
		}
	
		storedEnergy -= energyCost;
		inputStack.stackSize -= 1;
		this.setInventorySlotContents(PATTERN_INPUT_SLOT, inputStack);
		
	}

	private String patternName = null;
	
	public String getPatternName() {
		return this.patternName;
	}
	
	public void setPatternName(String text) {
		this.patternName = text;
		this.updateOutputSlot();
		
		if (this.worldObj.isRemote)
			RezolvePacketHandler.INSTANCE.sendToServer(new BundleBuilderUpdateMessage(this));
	}
}
