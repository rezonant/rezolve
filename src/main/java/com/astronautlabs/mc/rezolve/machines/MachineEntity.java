package com.astronautlabs.mc.rezolve.machines;

import cofh.api.energy.IEnergyReceiver;
import com.astronautlabs.mc.rezolve.common.*;
import com.astronautlabs.mc.rezolve.core.inventory.InventorySnapshot;
import com.astronautlabs.mc.rezolve.util.ItemStackUtil;
import com.astronautlabs.mc.rezolve.util.ItemUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MachineEntity extends TileEntityBase
		implements IInventory, ITickable, IEnergyReceiver, IMachineInventory, ICapabilityProvider {
	public MachineEntity(String registryName) {
		super(registryName);
		this.inventory = new ItemStack[this.getSizeInventory()];
		this.storedEnergy = 0;
	}

	protected ItemStack[] inventory;
	Operation<? extends MachineEntity> currentOperation;

	protected boolean hasCurrentOperation() {
		return this.currentOperation != null;
	}

	public int takeEnergy(int i) {
		int energyTaken = Math.min(i, this.storedEnergy);
		this.storedEnergy -= energyTaken;
		this.notifyUpdate();

		return energyTaken;
	}

	public Operation<? extends MachineEntity> getCurrentOperation() {
		return this.currentOperation;
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.hasCustomName() ? new TextComponentString(this.getName())
				: new TextComponentTranslation(this.getName());
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.getCustomName() : "container." + this.getRegistryName();
	}

	@Override
	public boolean hasCustomName() {
		return this.getCustomName() != null && !"".equals(this.getCustomName());
	}

	@Override
	public int getSizeInventory() {
		return 0;
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
				return itemstack;
			} else {
				ItemStack remainingItems = ItemStackUtil.cloneStack(this.getStackInSlot(index));
				itemstack = remainingItems.splitStack(count);

				if (this.getStackInSlot(index).stackSize <= 0) {
					this.setInventorySlotContents(index, null);
				} else {
					this.setInventorySlotContents(index, remainingItems);
				}

				return itemstack;
			}
		} else {
			return null;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return null;
	}

	public boolean allowInputToSlot(int index) {
		return true;
	}

	public boolean allowOutputFromSlot(int index) {
		return true;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index < 0 || index >= this.getSizeInventory())
			return;

		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
			stack.stackSize = this.getInventoryStackLimit();

		if (stack != null && stack.stackSize == 0)
			stack = null;

		ItemStack originalStack = this.inventory[index];

		if (!ItemStack.areItemStackTagsEqual(originalStack, stack) || !ItemStack.areItemStacksEqual(originalStack, stack)) {
			this.inventory[index] = stack;
			this.onSlotChanged(index, stack);
			this.markDirty();
			this.notifyUpdate();
		}
	}

	public void onSlotChanged(int index, ItemStack stack) {

	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return this.worldObj.getTileEntity(this.getPos()) == this
				&& player.getDistanceSq(this.pos.add(0.5, 0.5, 0.5)) <= 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	public Class<? extends MachineGui> getGuiClass() {
		return null;
	}

	public ContainerBase<?> createContainerFor(EntityPlayer player) {
		Class<? extends MachineGui> guiClass = getGuiClass();

		if (guiClass == null)
			return null;

		// String.class here is the parameter type, that might not be the case with you
		try {
			Method method = guiClass.getMethod("createContainerFor", EntityPlayer.class, MachineEntity.class);
			return (ContainerBase<?>)method.invoke(null, player, this);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException("Error: Machine GUI "+guiClass.getCanonicalName()+" does not have a public static createContainerFor() method.", e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error: Machine GUI "+guiClass.getCanonicalName()+" has a non-public createContainerFor() method.", e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException("This was unexpected", e);
		}
	}

	public GuiContainerBase createGuiFor(EntityPlayer player) {
		Class<? extends MachineGui> guiClass = getGuiClass();

		if (guiClass == null)
			return null;

		MachineGui gui;
		try {
			gui = guiClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error: Machine GUI "+guiClass.getCanonicalName()+": Failed to instantiate Gui subclass (must have parameterless constructor)", e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error: Machine GUI "+guiClass.getCanonicalName()+": Illegal access", e);
		}

		try {


			Method method = guiClass.getMethod("initialize", EntityPlayer.class, MachineEntity.class);
			method.setAccessible(true);
			method.invoke(gui, player, this);

			return gui;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new RuntimeException("Error: Machine GUI "+guiClass.getCanonicalName()+" must have initialize(EntityPlayer player, MachineEntity entity)", e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < this.getSizeInventory(); i++)
			this.setInventorySlotContents(i, null);
	}

	private long lastUpdate = 0;

	/**
	 * Fire an update every N ticks (20 ticks per second)
	 */
	protected long updateInterval = 10;

	/**
	 * This is run every `updateInterval` with the default implementation of
	 * update()
	 */
	public void updatePeriodically() {
		// TODO: implement in subclass
	}

	/**
	 * Start a new operation, called in the tick update method.
	 * 
	 * @return
	 */
	public Operation startOperation() {
		return null;
	}

	/**
	 * Manually start an operation
	 * 
	 * @param operation
	 * @return
	 */
	protected boolean startOperation(Operation operation) {
		if (this.currentOperation != null)
			return false;

		this.currentOperation = operation;
		System.out.println("Starting operation");
		this.notifyUpdate();
		return true;
	}

	protected InventorySnapshot createInventorySnapshot() {
		ItemStack[] slots = new ItemStack[this.inventory.length];
		for (int i = 0, max = this.inventory.length; i < max; ++i) {
			slots[i] = this.inventory[i] != null ? this.inventory[i].copy() : null;
		}

		return new InventorySnapshot(slots);
	}

	protected boolean applyInventorySnapshot(InventorySnapshot snapshot) {

		if (snapshot.getSlots().length != this.inventory.length)
			return false;

		ItemStack[] slots = new ItemStack[this.inventory.length];
		ItemStack[] snapshotSlots = snapshot.getSlots();

		for (int i = 0, max = this.inventory.length; i < max; ++i) {
			slots[i] = snapshotSlots[i] != null ? snapshotSlots[i].copy() : null;
		}

		this.inventory = slots;
		return true;
	}

	protected InventorySnapshot startInventoryTransaction() {
		return this.createInventorySnapshot();
	}

	protected void rollbackInventoryTransaction(InventorySnapshot snapshot) {
		this.applyInventorySnapshot(snapshot);
	}

	protected void commitInventoryTransaction(InventorySnapshot snapshot) {
		// noop
	}

	protected void updatePeriodicallyOnClient() {
		
	}
	
	@Override
	public void update() {

		long currentTime = this.worldObj.getTotalWorldTime();

		if (lastUpdate + updateInterval > currentTime)
			return;

		if (this.worldObj.isRemote) {
			this.updatePeriodicallyOnClient();
			return;
		}

		lastUpdate = currentTime;

		if (this.hasCurrentOperation()) {
			boolean finished = this.currentOperation.update();
			if (finished) {
				System.out.println("Operation completed.");
				this.currentOperation = null;
			}
			this.notifyUpdate();
		} else {
			Operation op = this.startOperation();
			if (op != null)
				this.startOperation(op);
		}

		this.updatePeriodically();

	}

	protected int maxEnergyStored = 20000;

	@Override
	public int getEnergyStored(EnumFacing arg0) {
		// TODO Auto-generated method stub
		return this.storedEnergy;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing arg0) {
		// TODO Auto-generated method stub
		return this.maxEnergyStored;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing arg0) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		int availableStorage = this.maxEnergyStored - this.storedEnergy;
		int receivedEnergy = Math.min(availableStorage, maxReceive);

		if (!simulate) {
			this.storedEnergy += receivedEnergy;
			this.notifyUpdate();
		}

		return receivedEnergy;
	}

	@Override
	public void outputSlotActivated(int index) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) new MachineItemHandler(this);
		}

		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	public int getSlots() {
		return this.getSizeInventory();
	}

	protected boolean allowedToPullFrom(int slot) {
		return true;
	}

	protected boolean allowedToPushTo(int slot) {
		return true;
	}

	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!this.allowedToPushTo(slot))
			return stack;

		if (!this.isItemValidForSlot(slot, stack))
			return stack;

		ItemStack existingStack = this.getStackInSlot(slot);

		if (existingStack != null && !ItemUtil.areStacksSame(stack, existingStack)) {
			return stack;
		}

		int itemsToKeep = stack.stackSize;
		ItemStack returnStack = null;
		ItemStack keepStack = stack.copy();

		if (existingStack != null && existingStack.stackSize + stack.stackSize > this.getInventoryStackLimit()) {
			itemsToKeep = this.getInventoryStackLimit() - existingStack.stackSize;
			returnStack = keepStack.splitStack(stack.stackSize - itemsToKeep);
		}

		if (existingStack != null)
			keepStack.stackSize += existingStack.stackSize;

		if (!simulate) {
			this.setInventorySlotContents(slot, keepStack);
		}

		return returnStack;
	}

	public Operation createOperation() {
		return null;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (this.currentOperation != null) {
			nbt.setBoolean("HasOp", true);
			this.currentOperation.writeNBT(nbt);
		} else {
			nbt.setBoolean("HasOp", false);
		}

		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("HasOp") && compound.getBoolean("HasOp")) {
			if (this.currentOperation == null) {
				this.currentOperation = this.createOperation();
				if (this.currentOperation == null) {
					System.err.println("ERROR: Machine " + this.getClass().getCanonicalName()
							+ " has not implemented createOperation()!");
				}
			}
			this.currentOperation.readNBT(compound);
		} else {
			this.currentOperation = null;
		}

		super.readFromNBT(compound);
	}

	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (!this.allowedToPullFrom(slot))
			return null;

		ItemStack existingStack = this.getStackInSlot(slot);

		if (existingStack == null)
			return null;

		ItemStack keepStack = null;
		ItemStack returnStack = existingStack.copy();

		if (existingStack.stackSize > amount) {
			returnStack = existingStack.splitStack(amount);
			keepStack = existingStack;
		}

		if (!simulate) {
			this.setInventorySlotContents(slot, keepStack);
		}

		return existingStack;
	}
}
