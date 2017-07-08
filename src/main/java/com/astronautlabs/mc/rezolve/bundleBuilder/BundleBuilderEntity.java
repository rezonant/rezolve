package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.BundlerNBT;
import com.astronautlabs.mc.rezolve.common.IMachineInventory;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import com.astronautlabs.mc.rezolve.network.BundlerPacketHandler;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BundleBuilderEntity extends TileEntityBase implements IInventory, IMachineInventory, IEnergyReceiver {
	public BundleBuilderEntity() {
		this.inventory = new ItemStack[this.getSizeInventory()];
	}
	
	public static void register() {
		GameRegistry.registerTileEntity(BundleBuilderEntity.class, "bundle_builder_tile_entity");
	}

    private ItemStack[] inventory;
    private String customName;

    public static final int PATTERN_INPUT_SLOT = 0;
    public static final int PATTERN_OUTPUT_SLOT = 1;
    public static final int DYE_SLOT = 2;
    
    
    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }
    
    public String getCustomName() {
        return this.customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }
    
	@Override
	public String getName() {
	    return this.hasCustomName() ? this.customName : "container.bundle_builder_tile_entity";
	}

	@Override
	public boolean hasCustomName() {
	    return this.customName != null && !this.customName.equals("");
	}

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
		
		BundlerNBT.writeInventory(nbt, this, 3, 9);
		
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
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= this.getSizeInventory())
	        return null;
	    return this.inventory[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (this.getStackInSlot(index) != null) {
	        ItemStack itemstack;

	        if (this.getStackInSlot(index).stackSize <= count) {
	            itemstack = this.getStackInSlot(index);
	            this.setInventorySlotContents(index, null);
	            this.markDirty();
	            return itemstack;
	        } else {
	        	RezolveMod.instance().log("SPLITTING OUTPUT STACK!");
	        	ItemStack originalStack = this.getStackInSlot(index);
	            itemstack = originalStack.splitStack(count);
	            itemstack.setTagCompound(originalStack.getTagCompound());
	            
	            if (this.getStackInSlot(index).stackSize <= 0) {
	                this.setInventorySlotContents(index, null);
	            } else {
	                //Just to show that changes happened
	                this.setInventorySlotContents(index, this.getStackInSlot(index));
	            }

	            this.markDirty();
	            return itemstack;
	        }
	    } else {
	        return null;
	    }
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
	    if (index < 0 || index >= this.getSizeInventory())
	        return;

	    if (stack != null && stack.stackSize > this.getInventoryStackLimit())
	        stack.stackSize = this.getInventoryStackLimit();
	        
	    if (stack != null && stack.stackSize == 0)
	        stack = null;

	    this.inventory[index] = stack;
	    
	    if (index != PATTERN_OUTPUT_SLOT)
	    	this.updateOutputSlot();
	    
	    this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
	    return this.worldObj.getTileEntity(this.getPos()) == this && player.getDistanceSq(this.pos.add(0.5, 0.5, 0.5)) <= 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
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
	public int getField(int id) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clear() {
	    for (int i = 0; i < this.getSizeInventory(); i++)
	        this.setInventorySlotContents(i, null);
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
			BundlerPacketHandler.INSTANCE.sendToServer(new BundleBuilderUpdateMessage(this));
	}

	@Override
	public int getEnergyStored(EnumFacing arg0) {
		return this.storedEnergy;
	}

	int maxEnergyStored = 2000;
	
	@Override
	public int getMaxEnergyStored(EnumFacing arg0) {
		return this.maxEnergyStored;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing arg0) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing side, int maxReceive, boolean simulate) {
		int availableStorage = this.maxEnergyStored - this.storedEnergy;
		int receivedEnergy = Math.min(availableStorage, maxReceive);
		
		if (!simulate) {
			this.storedEnergy += receivedEnergy;
			this.notifyUpdate();
		}
		
		return receivedEnergy;
	}
}
