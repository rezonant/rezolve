package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RegistryId("machine_menu")
public class MachineMenuStatePacket extends RezolveMenuPacket {
    private static final Logger LOGGER = LogManager.getLogger(RezolveMod.MODID);

    public int energyCapacity;
    public int energyStored;
    public float progress;
    public Operation operation;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        energyCapacity = buf.readInt();
        energyStored = buf.readInt();
        progress = buf.readFloat();
        operation = buf.readBoolean() ? Operation.of(buf.readNbt()) : null;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeInt(energyCapacity);
        buf.writeInt(energyStored);
        buf.writeFloat(progress);
        buf.writeBoolean(operation != null);
        if (operation != null)
            buf.writeNbt(operation.writeNBT());
    }
}
