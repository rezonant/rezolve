package com.rezolvemc.common.machines;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.network.RezolveMenuPacket;
import com.rezolvemc.common.inventory.IngredientSlot;
import com.rezolvemc.common.inventory.SetIngredientSlotPacket;
import com.rezolvemc.common.registry.RezolveRegistry;
import org.torchmc.events.Event;
import org.torchmc.events.EventEmitter;
import org.torchmc.events.EventType;
import org.torchmc.inventory.StandardSlot;
import org.torchmc.inventory.VirtualInventory;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.network.RezolvePacketReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.NetworkDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.torchmc.util.Values;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MachineMenu<MachineT extends MachineEntity> extends AbstractContainerMenu implements RezolvePacketReceiver, EventEmitter {
	public static final int SLOT_PIXEL_SIZE = 18;
	private static final Logger LOGGER = LogManager.getLogger(Rezolve.ID);

	protected MachineMenu(int pContainerId, Inventory playerInventory, MachineT machine) {
		super(null, pContainerId);

		this.menuType = RezolveRegistry.menuType(this.getClass());
		this.playerInventory = playerInventory;
		this.player = playerInventory.player;
		this.machine = machine;
		this.container = machine != null ? machine : new VirtualInventory();

		if (machine != null)
			machine.addActiveMenu(this);

		this.setupProperties();
	}

	protected MachineMenu(MenuType<MachineMenu> menuType, int pContainerId, Inventory playerInventory) {
		this(pContainerId, playerInventory, null);
	}

	public static EventType<Event> READY = new EventType<>();
	public static EventType<PropertyEvent> PROPERTY_CHANGED = new EventType<>();
	public static EventType<Event> PROPERTIES_CHANGED = new EventType<>();

	private EventMap eventMap = new EventMap();
	public final Container container;
	protected Inventory playerInventory;
	protected Player player;
	protected MachineT machine;
	private List<SyncedProperty> syncedProperties;
	private Map<String, WeakReference<Object>> propertyValueCache = new HashMap<>();
	private Map<String, Tag> propertyTagCache = new HashMap<>();
	private boolean isReady = false;
	private int firstPlayerInventorySlot = -1;
	private List<PacketSubscriber> packetSubscribers = new ArrayList<>();

	@Sync public String dimension;
	@Sync public BlockPos blockPos;
	@Sync public int energyCapacity;
	@Sync public int energyStored;
	@Sync public float progress;
	@Sync public Operation operation;

	@Override
	public EventMap eventMap() {
		return eventMap;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		if (machine != null)
			machine.removeActiveMenu(this);
	}

	private record SyncedProperty(String name, Sync annotation, Field field) { }

	public MachineT getMachine() {
		return machine;
	}

	private void setupProperties() {
		List<SyncedProperty> props = new ArrayList<>();
		for (var field : getClass().getFields()) {
			var annotation = field.getAnnotation(Sync.class);
			if (annotation != null) {
				props.add(new SyncedProperty(field.getName(), annotation, field));
			}
		}

		this.syncedProperties = props;
	}

	private CompoundTag gatherPropertyChanges() {
		var newValues = new HashMap<String, Object>();
		var newTag = gatherPropertyState(newValues);
		CompoundTag changes = new CompoundTag();

		for (var key : newTag.getAllKeys()) {
			var changed = true;
			if (propertyValueCache.containsKey(key)) {
				var value = propertyValueCache.get(key).get();
				var newValue = newValues.get(key);
				changed = !Objects.equals(value, newValue);

				if (newValue instanceof INBTSerializable) {
					var newNBT = ((INBTSerializable<?>) newValue).serializeNBT();

					if (propertyTagCache.containsKey(key)) {
						var oldNBT = propertyTagCache.get(key);
						changed = !Objects.equals(oldNBT.getAsString(), newNBT.getAsString());
					} else {
						changed = true;
					}

					if (changed) {
						propertyTagCache.put(key, newNBT);
					}
				}

				if (value instanceof Operation) {
					changed = true;
				}
			}

			if (changed) {
				changes.put(key, newTag.get(key));
				propertyValueCache.put(key, new WeakReference<>(newValues.get(key)));
			}
		}

		return changes;
	}

	private CompoundTag gatherPropertyState(Map<String, Object> values) {
		CompoundTag tag = new CompoundTag();

		for (var property : syncedProperties) {
			Object value;
			try {
				value = property.field.get(this);
				values.put(property.name, value);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(String.format("Incorrect access set on %s, must be public or protected.", property.name), e);
			}


			var propertyClass = property.field.getType();

			if (Values.instanceOf(propertyClass, String.class))
				tag.putString(property.name, value == null ? "<NULL>" : (String)value);
			else if (Values.instanceOf(propertyClass, int.class))
				tag.putInt(property.name, (int)value);
			else if (Values.instanceOf(propertyClass, float.class))
				tag.putFloat(property.name, (float)value);
			else if (Values.instanceOf(propertyClass, double.class))
				tag.putDouble(property.name, (double)value);
			else if (Values.instanceOf(propertyClass, long.class))
				tag.putLong(property.name, (long)value);
			else if (Values.instanceOf(propertyClass, boolean.class))
				tag.putBoolean(property.name, (boolean)value);
			else if (Values.instanceOf(propertyClass, CompoundTag.class))
				tag.put(property.name, (CompoundTag)value);
			else if (Values.instanceOf(propertyClass, Operation.class))
				tag.put(property.name, Operation.asTag((Operation) value));
			else if (Values.instanceOf(propertyClass, Direction.class))
				tag.putString(property.name, ((Direction)value).name());
			else if (Values.instanceOf(propertyClass, BlockPos.class)) {
				if (value != null)
					tag.put(property.name, NbtUtils.writeBlockPos((BlockPos) value));
			} else if (Values.instanceOf(propertyClass, INBTSerializable.class)) {
				if (value == null)
					tag.putString(property.name, "<NULL>");
				else
					tag.put(property.name, ((INBTSerializable) value).serializeNBT());
			}
		}

		return tag;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		sendMachineStatePacket();
	}

	@Override
	public void broadcastFullState() {
		super.broadcastFullState();
		clearStateCache();
		sendMachineStatePacket();
	}

	protected void updateState() {
		if (this.machine != null) {
			dimension = this.machine.getLevel().dimension().location().toString();
			blockPos = this.machine.getBlockPos();

			energyCapacity = this.machine.getEnergyCapacity();
			energyStored = this.machine.getStoredEnergy();
			progress = this.machine.getProgress();
			operation = this.machine.getCurrentOperation();

			emitEvent(READY);
		}
	}

	private void sendMachineStatePacket() {
		this.updateState();

		var updates = this.gatherPropertyChanges();
		if (updates.isEmpty())
			return;

		var state = new MachineMenuStatePacket();
		state.setMenu(this);
		state.properties = updates;
		state.sendToPlayer((ServerPlayer) this.playerInventory.player);
	}

	private void clearStateCache() {
		this.propertyValueCache = new HashMap<>();
	}

	@Override
	public void receivePacketOnClient(RezolvePacket rezolvePacket) {
		if (rezolvePacket instanceof MachineMenuStatePacket state) {
			var tag = state.properties;
			for (var key : tag.getAllKeys()) {
				var property = syncedProperties.stream().filter(p -> Objects.equals(p.name, key)).findFirst().orElse(null);
				if (property == null)
					continue;

				var propertyClass = property.field.getType();
				Object value = null;

				if (Values.instanceOf(propertyClass, String.class)) {
					value = tag.getString(property.name);
					if (Objects.equals(value, "<NULL>"))
						value = null;
				} else if (Values.instanceOf(propertyClass, int.class))
					value = tag.getInt(property.name);
				else if (Values.instanceOf(propertyClass, float.class))
					value = tag.getFloat(property.name);
				else if (Values.instanceOf(propertyClass, double.class))
					value = tag.getDouble(property.name);
				else if (Values.instanceOf(propertyClass, long.class))
					value = tag.getLong(property.name);
				else if (Values.instanceOf(propertyClass, boolean.class))
					value = tag.getBoolean(property.name);
				else if (Values.instanceOf(propertyClass, CompoundTag.class))
					value = tag.getCompound(property.name);
				else if (Values.instanceOf(propertyClass, Operation.class))
					value = Operation.of(tag.getCompound(property.name));
				else if (Values.instanceOf(propertyClass, Direction.class))
					value = Direction.valueOf(tag.getString(property.name));
				else if (Values.instanceOf(propertyClass, BlockPos.class)) {
					if (tag.contains(property.name))
						value = NbtUtils.readBlockPos(tag.getCompound(property.name));
					else
						value = null;
				} else if (Values.instanceOf(propertyClass, INBTSerializable.class)) {
					var propTag = tag.get(property.name);
					if (propTag instanceof StringTag stringTag && Objects.equals("<NULL>", stringTag.getAsString())) {
						value = null;
					} else {
						try {
							var serializable = (INBTSerializable)propertyClass.getConstructor().newInstance();
							serializable.deserializeNBT(propTag);
							value = serializable;
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(
								String.format(
									"Failed to create instance of NBT-serializable class %s",
									propertyClass.getCanonicalName()
								), e
							);
						}
					}
				}

				try {
					property.field.set(this, value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(String.format("Incorrect access set on %s, must be public or protected.", property.name), e);
				}

				emitEvent(PROPERTY_CHANGED, new PropertyEvent(property.name, value));
			}

			emitEvent(PROPERTIES_CHANGED);

			if (!isReady) {
				isReady = true;
				emitEvent(READY);
			}
		} else {
			RezolvePacketReceiver.super.receivePacketOnClient(rezolvePacket);
		}
	}

	public void setIngredient(IngredientSlot slot, ItemStack stack) {
		var slotUpdate = new SetIngredientSlotPacket();
		slotUpdate.setMenu(this);
		slotUpdate.setSlot(slot);
		slotUpdate.stack = stack;
		slotUpdate.sendToServer();
	}

	@Override
	public void receivePacketOnServer(RezolvePacket rezolvePacket, Player player) {
		if (rezolvePacket instanceof SetIngredientSlotPacket slotUpdate) {
			if (slotUpdate.slotId < 0 || slotUpdate.slotId >= slots.size()) {
				LOGGER.error("Received ingredient slot packet for invalid slot ID {}", slotUpdate.slotId);
				return;
			}

			var slot = this.getSlot(slotUpdate.slotId);
			if (slot instanceof IngredientSlot ingredientSlot) {
				if (!ingredientSlot.isValidItem(slotUpdate.stack))
					return;

				if (ingredientSlot.isSingleItemOnly())
					slotUpdate.stack.setCount(1);

				ingredientSlot.set(slotUpdate.stack);
			} else {
				LOGGER.error("Received ingredient slot packet for slot ID {} but slot is not a ghost slot (its type is {})", slotUpdate.slotId, slot.getClass().getCanonicalName());
				return;
			}

		} else {
			RezolvePacketReceiver.super.receivePacketOnServer(rezolvePacket, player);
		}
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int sourceSlotId) {
		if (sourceSlotId < 0 || sourceSlotId > slots.size())
			return ItemStack.EMPTY;

		var sourceSlot = getSlot(sourceSlotId);
		var quickMovedStack = sourceSlot.getItem().copy();

		if (sourceSlot instanceof IngredientSlot)
			return ItemStack.EMPTY;

		// Try adding to an existing slot.

		for (int destinationSlotId = 0, max = slots.size(); destinationSlotId < max; ++destinationSlotId) {
			// Not interested in moving from machine slot to machine slot
			if (sourceSlotId < firstPlayerInventorySlot && destinationSlotId < firstPlayerInventorySlot)
				continue;

			// Not interested in moving from player slot to player slot
			if (sourceSlotId >= firstPlayerInventorySlot && destinationSlotId >= firstPlayerInventorySlot)
				continue;

			var destinationSlot = getSlot(destinationSlotId);
			var existingStack = destinationSlot.getItem().copy();

			if (destinationSlot instanceof IngredientSlot)
				continue;

			if (ItemStack.isSame(existingStack, quickMovedStack)) {
				var takeAmount = Math.min(quickMovedStack.getCount(), destinationSlot.getItem().getMaxStackSize() - destinationSlot.getItem().getCount());
				if (takeAmount <= 0)
					continue;

				var taken = quickMovedStack.split(takeAmount);
				existingStack.setCount(existingStack.getCount() + taken.getCount());

				destinationSlot.set(existingStack);
				sourceSlot.set(quickMovedStack);
				return quickMovedStack;
			}
		}

		// Try looking for an empty slot.

		for (int destinationSlotId = 0, max = slots.size(); destinationSlotId < max; ++destinationSlotId) {
			// Not interested in moving from machine slot to machine slot
			if (sourceSlotId < firstPlayerInventorySlot && destinationSlotId < firstPlayerInventorySlot)
				continue;

			// Not interested in moving from player slot to player slot
			if (sourceSlotId >= firstPlayerInventorySlot && destinationSlotId >= firstPlayerInventorySlot)
				continue;

			var destinationSlot = getSlot(destinationSlotId);
			var existingStack = destinationSlot.getItem().copy();

			if (existingStack.isEmpty()) {
				if (destinationSlot instanceof IngredientSlot ingredientSlot) {

					if (ingredientSlot.isSingleItemOnly())
						destinationSlot.set(new ItemStack(quickMovedStack.getItem()));
					else
						destinationSlot.set(quickMovedStack);

					return ItemStack.EMPTY;
				} else if (destinationSlot.mayPlace(quickMovedStack)) {
					destinationSlot.set(quickMovedStack);
					sourceSlot.set(ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.machine != null ? this.machine.stillValid(pPlayer) : true;
	}

	public boolean hasPlayerInventorySlots() {
		return firstPlayerInventorySlot >= 0;
	}

	public int getFirstPlayerInventorySlot() {
		return firstPlayerInventorySlot;
	}

	protected void addSlotGrid(int firstSlot, int gridStride, int gridHeight) {
		addSlotGrid(firstSlot, id -> new StandardSlot(container, id), gridStride, gridHeight);
	}

	protected void addSlotGrid(int gridStride, int gridHeight) {
		addSlotGrid(slots.size(), gridStride, gridHeight);
	}

	protected void addSlotGrid(int firstSlot, BiFunction<Integer, Integer, Slot> ctor, int gridStride, int gridHeight) {
		int slotSize = 18;
		for (int j = 0, maxJ = gridHeight; j < maxJ; ++j) {
			for (int i = 0, maxI = gridStride; i < maxI; ++i) {
				addSlot(ctor.apply(firstSlot + j * gridStride + i, j * gridStride + i));
			}
		}
	}

	protected void addSlotGrid(int firstSlot, Function<Integer, Slot> ctor, int gridStride, int gridHeight) {
		addSlotGrid(firstSlot, (id, index) -> ctor.apply(id), gridStride, gridHeight);
	}

	protected void addSlotGrid(BiFunction<Integer, Integer, Slot> ctor, int gridStride, int gridHeight) {
		addSlotGrid(slots.size(), ctor, gridStride, gridHeight);
	}

	protected void addSlotGrid(Function<Integer, Slot> ctor, int gridStride, int gridHeight) {
		addSlotGrid(slots.size(), ctor, gridStride, gridHeight);
	}

	protected void addPlayerSlots() {
		firstPlayerInventorySlot = slots.size();
		addSlotGrid((id, i) -> new StandardSlot(playerInventory, 9 + i), 9, 3);
		addSlotGrid((id, i) -> new StandardSlot(playerInventory, i), 9, 1);
	}

	public interface PacketSubscriber {
		boolean handlePacket(RezolveMenuPacket packet);
	}

	public void addPacketHandler(PacketSubscriber subscriber) {
		packetSubscribers.add(subscriber);
	}

	@Override
	public void receivePacket(RezolvePacket rezolvePacket, NetworkDirection direction) {
		for (var subscriber : packetSubscribers) {
			if (subscriber.handlePacket((RezolveMenuPacket)rezolvePacket))
				return;
		}

		RezolvePacketReceiver.super.receivePacket(rezolvePacket, direction);
	}

	public static class PropertyEvent extends Event {
		public PropertyEvent(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public final String name;
		public final Object value;
	}
}