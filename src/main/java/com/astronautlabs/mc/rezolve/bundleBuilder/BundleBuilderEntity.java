package com.astronautlabs.mc.rezolve.bundleBuilder;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.*;
import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BundleBuilderEntity extends MachineEntity {
    public static final int PATTERN_INPUT_SLOT = 0;
    public static final int PATTERN_OUTPUT_SLOT = 1;
    public static final int DYE_SLOT = 2;
	public static final String ID = "bundle_builder";

	public BundleBuilderEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
		maxEnergyStored = 20000;

		addSlot(new BundlePatternSlot(this, PATTERN_INPUT_SLOT, 0, 0));
		addSlot(new OutputSlot(this, PATTERN_OUTPUT_SLOT, 0, 0));
		addSlot(new DyeSlot(this, DYE_SLOT, 0, 0));

		// Item inventory
		for (int i = this.getSlotCount(), max = 9; i < 0; ++i)
			addSlot(new Slot(this, i, 0, 0));

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
		
		if (this.getStoredEnergy() < this.energyCost)
			clearOutputSlot = true;
		
		if (clearOutputSlot) {
			this.setItem(PATTERN_OUTPUT_SLOT, null);
		} else {
			this.producePattern();
		}
		
	}
	
	private int energyCost = 100;
	
	private void producePattern() {
		ItemStack stack = new ItemStack(RezolveRegistry.item(BundlePatternItem.class), 1);
		CompoundTag nbt = new CompoundTag();
		
		RezolveNBT.writeInventory(nbt, this.itemHandler, 3, 9);
		
		if (this.patternName != null)
			nbt.putString("Name", this.patternName);
		
		int dye = this.dyeValue();
		if (dye != 0)
			nbt.putInt("Color", this.dyeValue());
		
		stack.setTag(nbt);

		this.setItem(PATTERN_OUTPUT_SLOT, stack);
	}
	
	public int dyeValue() {
		ItemStack stack = this.getStackInSlot(DYE_SLOT);
		
		if (stack == null || stack.getCount() == 0)
			return -1;
		
		return stack.getItem().getDamage(stack);
	}
	
	public boolean hasInputItems() {
		for (int i = 3, max = this.getSlotCount(); i < max; ++i) {
			ItemStack stack = this.getStackInSlot(i);
			if (stack != null && stack.getCount() > 0)
				return true;
		}
		
		return false;
	}
	
	public boolean hasBlankPattern() {
		ItemStack inputStack = this.getStackInSlot(PATTERN_INPUT_SLOT);
		
		if (inputStack == null)
			return false;
		
		if (inputStack.getCount() == 0)
			return false;
		
		return true;
	}

	@Override
	public ItemStack insertItem(int slotId, ItemStack stack, boolean simulate) {
		if (simulate)
			return super.insertItem(slotId, stack, simulate);

		if (slotId == PATTERN_INPUT_SLOT) {

			if (stack != null && !RezolveRegistry.item(BundlePatternItem.class).isBlank(stack)) {
				// A non-blank pattern was put into the input slot.
				// Transform it into a blank slot and set up the other inventory slots according to the pattern.

				// Clear existing items
				for (int i = 0, max = this.getSlotCount(); i < max; ++i) {
					if (i == PATTERN_INPUT_SLOT || i == PATTERN_OUTPUT_SLOT || i == DYE_SLOT)
						continue;

					this.setItem(i, null);
				}

				if (stack.hasTag()) {
					VirtualInventory vinv = new VirtualInventory();
					RezolveNBT.readInventory(stack.getTag(), vinv);

					int slot = 0;
					for (ItemStack patternStack : vinv.getStacks()) {
						if (patternStack == null)
							System.out.println("Handling a stack of NULL");
						else
							System.out.println("Handling a stack of "+patternStack.getItem().toString());

						this.setItem(3 + slot++, patternStack);
					}

					int dyeValue = stack.getTag().getInt("Color");

					if (dyeValue >= 0)
						this.setItem(DYE_SLOT, new ItemStack(DyeItem.byColor(DyeColor.byId(dyeValue)), 1));
					else
						this.setItem(DYE_SLOT, null);
				}

				stack = RezolveRegistry.item(BundlePatternItem.class).blank(stack.getCount());
			}
		}

		ItemStack result = super.insertItem(slotId, stack, simulate);

		if (slotId != PATTERN_OUTPUT_SLOT) {
			this.updateOutputSlot();
		}

		return result;
	}

	@Override
	protected boolean allowedToPullFrom(int slot) {
		return false;
	}
	
	@Override
	public void outputSlotActivated(int index) {
		
		// Consume a single pattern
		
		ItemStack inputStack = this.getStackInSlot(PATTERN_INPUT_SLOT);
		if (inputStack == null) {
			return;
		}

		this.energy.extractEnergy(energyCost, true); // TODO: negative
		inputStack.setCount(inputStack.getCount() - 1);
		this.setItem(PATTERN_INPUT_SLOT, inputStack);
		
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
		
//		if (this.level.isClientSide)
//			RezolvePacketHandler.INSTANCE.sendToServer(new BundleBuilderUpdateMessage(this));
	}
}
