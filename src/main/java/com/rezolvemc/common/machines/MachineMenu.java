package com.rezolvemc.common.machines;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.Errors;
import org.torchmc.RezolveMenuPacket;
import com.rezolvemc.common.inventory.IngredientSlot;
import com.rezolvemc.common.inventory.SetIngredientSlotPacket;
import com.rezolvemc.common.inventory.VirtualInventory;
import com.rezolvemc.common.network.RezolvePacket;
import com.rezolvemc.common.network.RezolvePacketReceiver;
import com.rezolvemc.common.network.WithPacket;
import net.minecraft.client.Minecraft;
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
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;

@WithPacket(MachineMenuStatePacket.class)
@WithPacket(SetIngredientSlotPacket.class)
public class MachineMenu<MachineT extends MachineEntity> extends AbstractContainerMenu implements RezolvePacketReceiver {
	private static final Logger LOGGER = LogManager.getLogger(Rezolve.ID);

	protected MachineMenu(MenuType<?> menuType, int pContainerId, Inventory playerInventory, MachineT machine) {
		super(menuType, pContainerId);

		this.playerInventory = playerInventory;
		this.machine = machine;
		this.container = machine != null ? machine : new VirtualInventory();

		this.setupProperties();
	}

	public final Container container;

	protected MachineMenu(MenuType<MachineMenu> menuType, int pContainerId, Inventory playerInventory) {
		this(menuType, pContainerId, playerInventory, null);
	}

	protected Inventory playerInventory;
	protected MachineT machine;

	private record SyncedProperty(String name, Sync annotation, Field field) { }

	private List<SyncedProperty> syncedProperties;

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

	private boolean instanceOf(Class<?> subclass, Class<?> superclass) {
		return superclass.isAssignableFrom(subclass);
	}

	private Map<String, WeakReference<Object>> propertyValueCache = new HashMap<>();
	private Map<String, Tag> propertyTagCache = new HashMap<>();

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

			if (instanceOf(propertyClass, String.class))
				tag.putString(property.name, value == null ? "<NULL>" : (String)value);
			else if (instanceOf(propertyClass, int.class))
				tag.putInt(property.name, (int)value);
			else if (instanceOf(propertyClass, float.class))
				tag.putFloat(property.name, (float)value);
			else if (instanceOf(propertyClass, double.class))
				tag.putDouble(property.name, (double)value);
			else if (instanceOf(propertyClass, long.class))
				tag.putLong(property.name, (long)value);
			else if (instanceOf(propertyClass, boolean.class))
				tag.putBoolean(property.name, (boolean)value);
			else if (instanceOf(propertyClass, CompoundTag.class))
				tag.put(property.name, (CompoundTag)value);
			else if (instanceOf(propertyClass, Operation.class))
				tag.put(property.name, Operation.asTag((Operation) value));
			else if (instanceOf(propertyClass, Direction.class))
				tag.putString(property.name, ((Direction)value).name());
			else if (instanceOf(propertyClass, BlockPos.class))
				tag.put(property.name, NbtUtils.writeBlockPos((BlockPos)value));
			else if (instanceOf(propertyClass, INBTSerializable.class)) {
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

	@Sync public int energyCapacity;
	@Sync public int energyStored;
	@Sync public float progress;
	@Sync public Operation operation;

	protected void updateState() {
		energyCapacity = this.machine.maxEnergyStored;
		energyStored = this.machine.getStoredEnergy();
		progress = this.machine.getProgress();
		operation = this.machine.getCurrentOperation();
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

				if (instanceOf(propertyClass, String.class)) {
					value = tag.getString(property.name);
					if (Objects.equals(value, "<NULL>"))
						value = null;
				} else if (instanceOf(propertyClass, int.class))
					value = tag.getInt(property.name);
				else if (instanceOf(propertyClass, float.class))
					value = tag.getFloat(property.name);
				else if (instanceOf(propertyClass, double.class))
					value = tag.getDouble(property.name);
				else if (instanceOf(propertyClass, long.class))
					value = tag.getLong(property.name);
				else if (instanceOf(propertyClass, boolean.class))
					value = tag.getBoolean(property.name);
				else if (instanceOf(propertyClass, CompoundTag.class))
					value = tag.getCompound(property.name);
				else if (instanceOf(propertyClass, Operation.class))
					value = Operation.of(tag.getCompound(property.name));
				else if (instanceOf(propertyClass, Direction.class))
					value = Direction.valueOf(tag.getString(property.name));
				else if (instanceOf(propertyClass, BlockPos.class))
					value = NbtUtils.readBlockPos(tag.getCompound(property.name));
				else if (instanceOf(propertyClass, INBTSerializable.class)) {
					var propTag = tag.get(property.name);
					if (propTag instanceof StringTag stringTag && Objects.equals("<NULL>", stringTag.getAsString())) {
						value = null;
					} else {
						try {
							var serializable = (INBTSerializable)propertyClass.getConstructor().newInstance();
							serializable.deserializeNBT(propTag);
							value = serializable;
						} catch (ReflectiveOperationException e) {
							Errors.reportException(Minecraft.getInstance().level, e, "While deserializing %s", propertyClass.getCanonicalName());

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
	public void receivePacketOnServer(RezolvePacket rezolvePacket) {
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
			RezolvePacketReceiver.super.receivePacketOnServer(rezolvePacket);
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
				if (takeAmount == 0)
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

	public static final int SLOT_PIXEL_SIZE = 18;

	private int firstPlayerInventorySlot = -1;

	public boolean hasPlayerInventorySlots() {
		return firstPlayerInventorySlot >= 0;
	}

	public int getFirstPlayerInventorySlot() {
		return firstPlayerInventorySlot;
	}

	protected void addSlotGrid(int firstSlot, int x, int y, int gridStride, int gridHeight) {
		addSlotGrid(firstSlot, (id, sx, sy) -> new Slot(container, id, sx, sy), x, y, gridStride, gridHeight);
	}

	protected void addSlotGrid(int firstSlot, TriFunction<Integer, Integer, Integer, Slot> ctor, int x, int y, int gridStride, int gridHeight) {
		int slotSize = 18;
		for (int j = 0, maxJ = gridHeight; j < maxJ; ++j) {
			for (int i = 0, maxI = gridStride; i < maxI; ++i) {
				addSlot(ctor.apply(j * gridStride + i, x + i * slotSize + 1, y + j * slotSize + 1));
			}
		}
	}

	protected void addPlayerSlots(int offsetX, int offsetY) {
		firstPlayerInventorySlot = this.slots.size();
		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 9; ++x) {
				this.addSlot(new Slot(playerInventory, 9 + x + y * 9, offsetX + x * SLOT_PIXEL_SIZE, offsetY + y * SLOT_PIXEL_SIZE));
			}
		}

		int playerHotbarOffsetX = offsetX;
		int playerHotbarOffsetY = offsetY + 58;

		// Player Hotbar, slots 0-8

		for (int x = 0; x < 9; ++x) {
			this.addSlot(new Slot(playerInventory, x, playerHotbarOffsetX + x * 18, playerHotbarOffsetY));
		}
	}

	public interface PacketSubscriber {
		boolean handlePacket(RezolveMenuPacket packet);
	}

	private List<PacketSubscriber> packetSubscribers = new ArrayList<>();

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
}