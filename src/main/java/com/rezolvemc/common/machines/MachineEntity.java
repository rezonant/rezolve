package com.rezolvemc.common.machines;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.blocks.BlockEntityBase;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.util.RezolveTagUtil;
import net.minecraft.server.level.ServerPlayer;
import org.torchmc.events.Event;
import org.torchmc.events.EventEmitter;
import org.torchmc.events.EventType;
import org.torchmc.events.Subscription;
import org.torchmc.inventory.InventorySnapshot;
import com.rezolvemc.common.network.RezolvePacketReceiver;
import com.rezolvemc.thunderbolt.cable.CableNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MachineEntity extends BlockEntityBase implements Container, IMachineInventory, ICapabilityProvider, RezolvePacketReceiver, GameEventListener, EventEmitter {
	private static final Logger LOGGER = LogManager.getLogger();

	public MachineEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
		this.energy = new MachineEnergyStorage(20000, 20000);
		this.itemHandler = new MachineItemHandler(this);
	}

	public static EventType<Event> UNLOADED = new EventType<>();

	protected Operation currentOperation;
	protected MachineEnergyStorage energy;
	protected MachineItemHandler itemHandler;
	private List<MachineMenu> activeMenus = new ArrayList<>();
	private EventMap eventMap = new EventMap();

	@Override
	public EventMap eventMap() {
		return eventMap;
	}

	void addActiveMenu(MachineMenu menu) {
		activeMenus.add(menu);
	}

	void removeActiveMenu(MachineMenu menu) {
		activeMenus.remove(menu);
	}

	public void removeWhenDestroyed(Subscription subscription) {
		listenForNextEvent(UNLOADED, e -> subscription.unsubscribe());
	}

	public MachineMenu[] getActiveMenus() {
		return activeMenus.toArray(new MachineMenu[activeMenus.size()]);
	}

	public void  sendPacketToActivePlayers(RezolvePacket packet) {
		for (var menu : activeMenus) {
			packet.sendToPlayer((ServerPlayer) menu.getPlayer());
		}
	}

	public int getEnergyCapacity() {
		return energy.getMaxEnergyStored();
	}

	protected void setEnergyCapacity(int capacity) {
		energy.setCapacity(capacity);
	}
	
	public int getMaxEnergyTransfer() {
		return this.getEnergyCapacity();
	}

	public int getStoredEnergy() {
		return this.energy.getEnergyStored();
	}

	protected boolean hasCurrentOperation() {
		return this.currentOperation != null;
	}

	public Operation getCurrentOperation() {
		return this.currentOperation;
	}

	public int takeEnergy(int energy) {
		return this.energy.extractEnergy(energy, false);
	}

	private List<CableNetwork> networks = new ArrayList<>();

	@Override
	public PositionSource getListenerSource() {
		return new BlockPositionSource(getBlockPos());
	}

	@Override
	public int getListenerRadius() {
		return 5;
	}

	@Override
	public boolean handleGameEvent(ServerLevel pLevel, GameEvent.Message pEventMessage) {
		if (pEventMessage.gameEvent() == GameEvent.BLOCK_DESTROY) {

			// Vec3.atCenterOf() always just adds 0.5 to all components, which means if you simply truncate to int
			// you will get the wrong result. Consider a block at Z -60. -60 + 0.5 = -59.5. Truncate(-59.5) = -59
			// But that's not the block position.

			var blockPos = new BlockPos(
					(int)(pEventMessage.source().x() - 0.5),
					(int)(pEventMessage.source().y() - 0.5),
					(int)(pEventMessage.source().z() - 0.5)
			);
			if (Objects.equals(getBlockPos(), blockPos)) {
				// uh oh, this is me
				wasDestroyed();
			}

			var networks = getNetworks();
			for (var network : networks) {
				var endpoint = network.getEndpoint(getLevel(), blockPos);
				if (endpoint != null) {
					// This block is an endpoint on the network, so we need to rebuild the network to address this change.
					network.invalidate();
				}
			}
		}

		return false;
	}

	/**
	 * Called when the corresponding block is destroyed, but not when the block entity is unloaded.
	 */
	protected void wasDestroyed() {
		// Invalidate all connected networks
		var networks = getNetworks();
		for (var network : networks) {
			network.invalidate();
		}
	}

	/**
	 * When true, this entity will act as a cable for the purposes of network planning.
	 * Most machines do not do this, but notably since Thunderbolt cables are machines,
	 * they do. Machines which act as cable can only be adopted by a single network (getNetworks() always
	 * returns a single item, and adoptNetwork() will remove the previously adopted networks, even when
	 * they are still valid), whereas machines that do not act as a cable can be adopted by multiple networks.
	 * @return
	 */
	public boolean actsAsCable() {
		return false;
	}

	/**
	 * Adopt the given network as our own. If this entity acts as a cable, then the attached network is replaced.
	 * Otherwise, the network is added to the list of valid networks.
	 * @param network
	 */
	public void adoptNetwork(CableNetwork network) {
		if (networks.contains(network))
			return;

		if (actsAsCable()) {
			// A cable can only ever be connected to a single network.
			// If another network is asking us to adopt it, then that means any existing network we
			// were associated with is no longer valid. In most cases, the existing network will have already
			// been invalidated, because of neighbor checking, block destroyed game events etc. However,
			// since Rezolve cables work across dimensions, it is possible for activity on the other end of the
			// dimensional link to cause a network to boot, and then traverse into the other dimension where a cable
			// network is already booted.

			for (var oldNetwork : networks)
				oldNetwork.invalidate();

			networks.clear();
		}
		this.networks.add(network);
	}

	public CableNetwork getExistingNetwork() {
		return networks.stream().filter(n -> !n.isInvalidated()).findFirst().orElse(null);
	}

	public CableNetwork[] getNetworks() {
		var filteredNetworks = networks.stream().filter(n -> !n.isInvalidated()).toList();
		networks.clear();
		networks.addAll(filteredNetworks);
		return networks.toArray(new CableNetwork[networks.size()]);
	}

	public CableNetwork getNetwork() {
		CableNetwork network = getExistingNetwork();
		if (actsAsCable() && network == null)
			adoptNetwork(network = CableNetwork.boot(level, getBlockPos()));

		return network;
	}

	//	@Override
//	public ITextComponent getDisplayName() {
//		return this.hasCustomName() ? new TextComponentString(this.getName())
//				: new TextComponentTranslation(this.getName());
//	}
//
//	@Override
//	public String getName() {
//		return this.hasCustomName() ? this.getCustomName() : "container." + this.getRegistryName();
//	}
//
//	@Override
//	public boolean hasCustomName() {
//		return this.getCustomName() != null && !"".equals(this.getCustomName());
//	}

	private List<Slot> slots = new ArrayList<>();
	private List<ItemStack> items = new ArrayList<>();

	protected void addSlot(Slot slot) {
		this.slots.add(slot);
		this.items.add(ItemStack.EMPTY.copy());
	}

	protected ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= this.items.size())
			return null;
		return this.items.get(index);
	}

	public boolean allowInputToSlot(int index) {
		return true;
	}

	public boolean allowOutputFromSlot(int index) {
		return true;
	}

	public Component getMenuTitle() {
		return Component.translatable("block.rezolve.machine");
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
		this.setChanged();
		return true;
	}

	protected InventorySnapshot createInventorySnapshot() {
		ItemStack[] slots = new ItemStack[this.items.size()];
		for (int i = 0, max = this.items.size(); i < max; ++i) {
			slots[i] = this.items.get(i) != null ? this.items.get(i).copy() : null;
		}

		return new InventorySnapshot(slots);
	}

	protected boolean applyInventorySnapshot(InventorySnapshot snapshot) {

		if (snapshot.getSlots().length != this.items.size())
			return false;

		ItemStack[] snapshotSlots = snapshot.getSlots();
		for (int i = 0, max = this.items.size(); i < max; ++i) {
			items.set(i, snapshotSlots[i] != null ? snapshotSlots[i].copy() : null);
		}

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

	public static void tick(Level level, BlockPos pos, BlockState state, MachineEntity entity) {
		entity.tick();
	}

	public void tick() {
		long currentTime = this.level.getGameTime();

		if (lastUpdate + updateInterval > currentTime)
			return;

		if (this.level.isClientSide) {
			this.updatePeriodicallyOnClient();
			return;
		}

		lastUpdate = currentTime;

		if (this.hasCurrentOperation()) {
			this.currentOperation.updateProgress();
			boolean finished = this.currentOperation.update();
			if (finished) {
				LOGGER.info("Operation completed on {}", getClass().getCanonicalName());
				this.currentOperation = null;
			}
			this.setChanged();
		} else {
			Operation op = this.startOperation();
			if (op != null)
				this.startOperation(op);
		}

		this.updatePeriodically();

	}

	@Override
	public void outputSlotActivated(int index) {
	}

	public boolean hasNetwork() {
		return getNetworks().length > 0;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == ForgeCapabilities.ITEM_HANDLER) {
			return LazyOptional.of(() -> (T) itemHandler);
		}

		if (capability == ForgeCapabilities.ENERGY) {
			return LazyOptional.of(() -> (T) this.energy);
		}

		return super.getCapability(capability, facing);
	}

	public int getSlotCount() {
		return this.slots.size();
	}

	protected boolean allowedToPullFrom(int slotId) {
		return allowedToPullFrom(slotId, null);
	}

	protected boolean allowedToPullFrom(int slotId, Player player) {
		var slot = getSlot(slotId);
		if (slot == null)
			return false;

		return slot.mayPickup(player);
	}

	protected Slot getSlot(int slotId) {
		if (slotId < 0 || slotId >= this.slots.size())
			return null;

		return slots.get(slotId);
	}

	protected boolean allowedToPushTo(int slotId, ItemStack stack) {
		var slot = getSlot(slotId);
		if (slot == null)
			return false;

		return slot.mayPlace(stack);
	}

	public ItemStack insertItem(int slotId, ItemStack stack, boolean simulate) {
		if (!this.allowedToPushTo(slotId, stack))
			return stack;

		ItemStack existingStack = this.getStackInSlot(slotId);
		Slot slot = this.getSlot(slotId);

		if (!existingStack.isEmpty() && !Rezolve.areStacksSame(stack, existingStack)) {
			return stack;
		}

		int takeCount = stack.getCount();

		takeCount = Math.min(slot.getMaxStackSize() - existingStack.getCount(), takeCount);

		if (!existingStack.isEmpty())
			takeCount = Math.min(existingStack.getItem().getMaxStackSize(existingStack) - existingStack.getCount(), takeCount);

		ItemStack returnStack = stack.copy();
		ItemStack takeStack = returnStack.split(takeCount);

		if (takeCount == 0) {
			return returnStack;
		}

		if (!simulate) {
			if (!existingStack.isEmpty())
				takeStack.setCount(takeStack.getCount() + existingStack.getCount());
			this.items.set(slotId, takeStack);
			fireOnSlotChanged(slot);
		}

		return returnStack;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (this.currentOperation != null)
			tag.put("op", this.currentOperation.asTag());

		if (this.customName != null)
			tag.putString("customName", this.getCustomName());

		tag.put("energy", this.energy.serializeNBT());
		RezolveTagUtil.writeInventory(tag, this);
	}

	@Override
	public void load(CompoundTag tag) {
		currentOperation = Operation.of(tag.getCompound("op"));
		if (currentOperation != null)
			currentOperation.setMachine(this);

		if (tag.contains("customName", Tag.TAG_STRING))
			this.setCustomName(tag.getString("customName"));

		if (tag.contains("energy"))
			this.energy.deserializeNBT(tag.get("energy"));

		RezolveTagUtil.readInventory(tag, this);
		super.load(tag);
	}

	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (!this.allowedToPullFrom(slot))
			return null;

		ItemStack existingStack = this.getStackInSlot(slot);

		if (existingStack == null)
			return null;

		ItemStack keepStack = null;
		ItemStack returnStack = existingStack.copy();

		if (existingStack.getCount() > amount) {
			returnStack = existingStack.split(amount);
			keepStack = existingStack;
		}

		if (!simulate) {
			this.items.set(slot, keepStack);
		}

		return existingStack;
	}

	private String customName;

	public String getCustomName() {
		return this.customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	@Override
	public int getContainerSize() {
		return this.slots.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty())
				return false;
		}

		return true;
	}

	@Override
	public ItemStack getItem(int pSlot) {
		if (pSlot < 0 || pSlot > this.items.size())
			return ItemStack.EMPTY.copy();
		return this.items.get(pSlot) == null ? ItemStack.EMPTY.copy() : this.items.get(pSlot).copy();
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		if (pSlot < 0 || pSlot > this.items.size())
			return ItemStack.EMPTY.copy();

		var stack = this.items.get(pSlot);
		if (stack == null)
			stack = ItemStack.EMPTY.copy();

		var result = stack.split(pAmount);
		setChanged();
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		if (pSlot < 0 || pSlot > this.items.size())
			return ItemStack.EMPTY.copy();

		var stack = getItem(pSlot);
		this.items.set(pSlot, ItemStack.EMPTY.copy());
		return stack;
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		if (pSlot < 0 || pSlot > this.items.size())
			return;

		if (pStack == null)
			pStack = ItemStack.EMPTY;

		this.items.set(pSlot, pStack.copy());
		this.setChanged();
		fireOnSlotChanged(this.slots.get(pSlot));
	}

	/**
	 * Run code when a specific slot changes
	 * @param slot
	 */
	protected void onSlotChanged(Slot slot) {

	}

	private boolean alreadyHandlingSlotChange = false;
	private void fireOnSlotChanged(Slot slot) {
		if (alreadyHandlingSlotChange)
			return;

		try {
			alreadyHandlingSlotChange = true;
			onSlotChanged(slot);
		} finally {
			alreadyHandlingSlotChange = false;
		}
	}

	public boolean isCurrentEntity() {
		return this.level.getBlockEntity(this.getBlockPos()) == this;
	}

	@Override
	public boolean stillValid(Player player) {
		return isCurrentEntity() && player.distanceToSqr(Vec3.atCenterOf(this.getBlockPos())) <= 64;
	}

	@Override
	public void clearContent() {
		for (int i = 0, max = this.items.size(); i < max; ++i)
			this.items.set(i, ItemStack.EMPTY.copy());
	}

	public float getProgress() {
		if (this.currentOperation != null) {
			return this.currentOperation.computeProgress();
		}

		return 0;
	}

	public class MachineEnergyStorage extends EnergyStorage {
		public MachineEnergyStorage(int capacity, int maxTransfer) {
			super(capacity, maxTransfer);
		}

		void setCapacity(int capacity) {
			this.capacity = capacity;
		}

		void setMaxTransfer(int value) {
			this.maxExtract = value;
			this.maxReceive = value;
		}

		void setMaxExtract(int value) {
			this.maxExtract = value;
		}

		void setMaxReceive(int value) {
			this.maxReceive = value;
		}
	}
}
