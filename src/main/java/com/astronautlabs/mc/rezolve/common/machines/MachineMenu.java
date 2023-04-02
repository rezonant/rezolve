package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.inventory.IngredientSlot;
import com.astronautlabs.mc.rezolve.common.inventory.SetIngredientSlotPacket;
import com.astronautlabs.mc.rezolve.common.inventory.VirtualInventory;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacket;
import com.astronautlabs.mc.rezolve.common.network.RezolvePacketReceiver;
import com.astronautlabs.mc.rezolve.common.network.WithPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;

@WithPacket(MachineMenuStatePacket.class)
@WithPacket(SetIngredientSlotPacket.class)
public class MachineMenu<MachineT extends MachineEntity> extends AbstractContainerMenu implements RezolvePacketReceiver {
	private static final Logger LOGGER = LogManager.getLogger(RezolveMod.ID);

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

	private CompoundTag gatherPropertyChanges() {
		var newValues = new HashMap<String, Object>();
		var newTag = gatherPropertyState(newValues);
		CompoundTag changes = new CompoundTag();

		for (var key : newTag.getAllKeys()) {
			var changed = true;
			if (propertyValueCache.containsKey(key)) {
				var value = propertyValueCache.get(key).get();
				changed = !Objects.equals(value, newValues.get(key));

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
				else if (instanceOf(propertyClass, INBTSerializable.class)) {
					var propTag = tag.get(property.name);
					if (propTag instanceof StringTag stringTag && Objects.equals("<NULL>", stringTag.getAsString())) {
						value = null;
					} else {
						try {
							var serializable = (INBTSerializable)propertyClass.getDeclaredConstructor().newInstance();
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
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return null;
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.machine != null ? this.machine.stillValid(pPlayer) : true;
	}

	public static final int SLOT_PIXEL_SIZE = 18;

	protected void addPlayerSlots(int offsetX, int offsetY) {
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
}