package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.RezolveNBT;
import com.astronautlabs.mc.rezolve.common.VirtualInventory;
import com.astronautlabs.mc.rezolve.common.MachineEntity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
			return -1;
		
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
	    
	    if (index == PATTERN_INPUT_SLOT) {
	    	// Check if its not a blank pattern
	    	
	    	if (stack != null && !RezolveMod.bundlePatternItem.isBlank(stack)) {
	    		// A non-blank pattern was put into the input slot.
	    		// Transform it into a blank slot and set up the other inventory slots according to the pattern.
	    		
	    		// Clear existing items
	    		for (int i = 0, max = this.getSizeInventory(); i < max; ++i) {
	    			if (i == PATTERN_INPUT_SLOT || i == PATTERN_OUTPUT_SLOT || i == DYE_SLOT)
	    				continue;
	    			
	    			this.setInventorySlotContents(i, null);
	    		}
	    		
	    		if (stack.hasTagCompound()) {
		    		VirtualInventory vinv = new VirtualInventory();
		    		RezolveNBT.readInventory(stack.getTagCompound(), vinv);
		    		
		    		int slot = 0;
		    		for (ItemStack patternStack : vinv.getStacks()) {
		    			if (patternStack == null)
			    			System.out.println("Handling a stack of NULL");
		    			else
		    				System.out.println("Handling a stack of "+patternStack.getItem().getRegistryName());
		    			this.setInventorySlotContents(3 + slot++, patternStack);
		    		}

		    		int dyeValue = stack.getTagCompound().getInteger("Color");
		    		
		    		if (dyeValue >= 0)
		    			this.setInventorySlotContents(DYE_SLOT, new ItemStack(Items.DYE, 1, dyeValue));
		    		else 
		    			this.setInventorySlotContents(DYE_SLOT, null);
	    		}
	    		
	    		stack = RezolveMod.bundlePatternItem.blank(stack.stackSize);
	    	}
	    }

	    super.setInventorySlotContents(index, stack);
	    
	    if (index != PATTERN_OUTPUT_SLOT) {
	    	this.updateOutputSlot();
	    }
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {

		if (index == PATTERN_OUTPUT_SLOT) {
			
			// Automation should never push into the pattern output slot,
			// as the only reason to put an item in there is to edit an existing 
			// pattern, which is not possible with automation.
			
			return false;

		} else if (index == PATTERN_INPUT_SLOT) {
			
			return stack.getItem() == RezolveMod.bundlePatternItem;
			
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
		if (this.patternName == null && text == null)
			return;
		
		if (this.patternName != null && this.patternName.equals(text))
			return;
		
		this.patternName = text;
		this.updateOutputSlot();
		
		if (this.worldObj.isRemote)
			RezolvePacketHandler.INSTANCE.sendToServer(new BundleBuilderUpdateMessage(this));
	}
}
