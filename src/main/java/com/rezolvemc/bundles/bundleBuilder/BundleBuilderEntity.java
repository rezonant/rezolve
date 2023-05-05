package com.rezolvemc.bundles.bundleBuilder;

import com.rezolvemc.common.inventory.DyeSlot;
import org.torchmc.inventory.VirtualInventory;
import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.machines.MachineOutputSlot;
import com.rezolvemc.common.registry.RezolveRegistry;
import com.rezolvemc.common.util.RezolveTagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class BundleBuilderEntity extends MachineEntity {
    public static final int PATTERN_INPUT_SLOT = 0;
    public static final int PATTERN_OUTPUT_SLOT = 1;
    public static final int DYE_SLOT = 2;

	public BundleBuilderEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(BundleBuilderEntity.class), pPos, pBlockState);
		setEnergyCapacity(20000);

		addSlot(new BundlePatternSlot(this, PATTERN_INPUT_SLOT));
		addSlot(new MachineOutputSlot(this, PATTERN_OUTPUT_SLOT));
		addSlot(new DyeSlot(this, DYE_SLOT));

		// Item inventory
		for (int i = this.getSlotCount(), max = this.getSlotCount() + 9; i < max; ++i)
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

	@Override
	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.bundle_builder");
	}

	private void producePattern() {
		ItemStack stack = new ItemStack(RezolveRegistry.item(BundlePatternItem.class), 1);
		CompoundTag nbt = new CompoundTag();
		
		RezolveTagUtil.writeInventory(nbt, this.itemHandler, 3, 9);
		
		if (this.patternName != null)
			nbt.putString("Name", this.patternName);

		String dye = this.dyeValue();
		if (dye != null)
			nbt.putString("Color", dye);
		
		stack.setTag(nbt);

		this.setItem(PATTERN_OUTPUT_SLOT, stack);
	}
	
	public String dyeValue() {
		ItemStack stack = this.getStackInSlot(DYE_SLOT);
		
		if (stack == null || stack.getCount() == 0)
			return null;
		
		return ((DyeItem)stack.getItem()).getDyeColor().getName();
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
					RezolveTagUtil.readInventory(stack.getTag(), vinv);

					int slot = 0;
					for (ItemStack patternStack : vinv.getStacks()) {
						if (patternStack == null)
							System.out.println("Handling a stack of NULL");
						else
							System.out.println("Handling a stack of "+patternStack.getItem().toString());

						this.setItem(3 + slot++, patternStack);
					}

					String dyeValue = stack.getTag().getString("Color");

					if (dyeValue != null)
						this.setItem(DYE_SLOT, new ItemStack(DyeItem.byColor(DyeColor.byName(dyeValue, DyeColor.PINK)), 1));
					else
						this.setItem(DYE_SLOT, null);
				}

				stack = RezolveRegistry.item(BundlePatternItem.class).blank(stack.getCount());
			}
		}

		return super.insertItem(slotId, stack, simulate);
	}

	@Override
	protected void onSlotChanged(Slot slot) {
		if (slot.index != PATTERN_OUTPUT_SLOT)
			updateOutputSlot();
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
	private boolean lockPositions = false;
	
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

	public boolean arePositionsLocked() {
		return lockPositions;
	}

	public void setLockedPositions(boolean value) {
		lockPositions = value;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);

		if (patternName != null)
			tag.putString("patternName", patternName);
		tag.putBoolean("lockPositions", lockPositions);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		patternName = tag.getString("patternName");
		lockPositions = tag.getBoolean("lockPositions");
	}
}
