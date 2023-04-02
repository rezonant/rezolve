package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.network.FriendlyByteBuf;

@RegistryId("set_pattern_settings")
public class SetPatternSettingsPacket extends RezolveMenuPacket {
    public String name;
    public boolean lockPositions;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);

        name = buf.readUtf();
        lockPositions = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);

        buf.writeUtf(name);
        buf.writeBoolean(lockPositions);
    }
}
