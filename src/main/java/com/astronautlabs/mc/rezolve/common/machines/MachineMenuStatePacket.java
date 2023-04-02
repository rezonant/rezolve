package com.astronautlabs.mc.rezolve.common.machines;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RegistryId("machine_menu")
public class MachineMenuStatePacket extends RezolveMenuPacket {
    private static final Logger LOGGER = LogManager.getLogger(RezolveMod.ID);

    public int energyCapacity;
    public int energyStored;
    public float progress;
    public CompoundTag properties;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        energyCapacity = buf.readInt();
        energyStored = buf.readInt();
        progress = buf.readFloat();
        properties = buf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeInt(energyCapacity);
        buf.writeInt(energyStored);
        buf.writeFloat(progress);
        buf.writeNbt(properties);
    }
}
