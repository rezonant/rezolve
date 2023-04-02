package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract class Operation<T extends MachineEntity> {
	private static final Logger LOGGER = LogManager.getLogger(RezolveMod.ID);

	/**
	 * Construct without reference to the machine. Used for constructing client-replicated Operation instances.
	 * Important to include this constructor in your subclass, or else the operation won't reach the client, and your
	 * UI will not be able to render the screen based on the operation!
	 */
	public Operation() {
		this.machine = null;
	}

	/**
	 * Construct with a reference to the machine. Used for constructing server-side Operation instances, either
	 * in MachineEntity.startOperation() or elsewhere. Don't forget to add a parameterless constructor for the client!
	 * @param machine
	 */
	public Operation(T machine) {
		this.machine = machine;
	}
	
	private T machine;
	private float progress;

	public static Tag asTag(Operation value) {
		if (value == null) {
			var tag = new CompoundTag();
			tag.putString("id", "null");
			return tag;
		}

		return value.asTag();
	}

	public T getMachine() {
		return this.machine;
	}
	
	protected abstract CompoundTag writeNBT();
	protected abstract void readNBT(CompoundTag nbt);
	public abstract float computeProgress();

	float getProgress() {
		return progress;
	}

	public int getPercentage() {
		return (int)(this.getProgress() * 100.0f);
	}

	public final CompoundTag asTag() {
		var tag = new CompoundTag();
		tag.putString("id", RezolveRegistry.requireRegistryId(getClass()));
		tag.putFloat("progress", progress);
		tag.put("payload", writeNBT());
		return tag;
	}

	/**
	 * Process an update on this operation. 
	 * @return True if the operation is completed, false if the operation continues.
	 */
	public abstract boolean update();

	public final void updateProgress() {
		progress = computeProgress();
	}

	public static Operation of(CompoundTag operationTag) {
		if (operationTag == null)
			return null;

		var operationId = operationTag.getString("id");

		var payloadTag = operationTag.getCompound("payload");
		Operation operation;

		if (Objects.equals("null", operationId) || Objects.equals("", operationId)) {
			operation = null;
		} else {
			Class<? extends Operation> operationClass = RezolveRegistry.getOperationClass(operationId);
			if (operationClass == null) {
				LOGGER.error("Unregistered operation type {}! Ignoring!", operationId);
				operation = null;
			} else {
				try {
					operation = operationClass.getDeclaredConstructor().newInstance();
				} catch (ReflectiveOperationException e) {
					var message = String.format(
							"Failed to construct operation %s (did you forget to add a parameterless constructor?): %s",
							operationClass.getCanonicalName(), e.getMessage()
					);

					LOGGER.error(message);
					throw new RuntimeException(message, e);
				}

				operation.progress = operationTag.getFloat("progress");
				operation.readNBT(payloadTag);
			}
		}

		return operation;
	}

}
