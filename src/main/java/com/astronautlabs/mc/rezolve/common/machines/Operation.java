package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Operation<T extends MachineEntity> {
	private static final Logger LOGGER = LogManager.getLogger(RezolveMod.MODID);

	public Operation(T machine) {
		this.machine = machine;
	}
	
	private T machine;
	
	public T getMachine() {
		return this.machine;
	}
	
	protected abstract CompoundTag writeNBT();
	protected abstract void readNBT(CompoundTag nbt);
	public abstract int getPercentage();

	public final CompoundTag asTag() {
		var tag = new CompoundTag();
		tag.putString("id", RezolveRegistry.requireRegistryId(getClass()));
		tag.put("payload", writeNBT());
		return tag;
	}

	/**
	 * Process an update on this operation. 
	 * @return True if the operation is completed, false if the operation continues.
	 */
	public abstract boolean update();


	public static Operation of(CompoundTag operationTag) {
		if (operationTag == null)
			return null;

		var operationId = operationTag.getString("id");

		var payloadTag = operationTag.getCompound("payload");
		Operation operation;

		if ("null".equals(operationId)) {
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
					throw new RuntimeException(
							String.format(
									"Failed to construct operation %s: %s",
									operationClass.getCanonicalName(), e.getMessage()
							), e
					);
				}

				operation.readNBT(payloadTag);
			}
		}

		return operation;
	}

}
