package com.rezolvemc.bundles.bundleBuilder;

import com.rezolvemc.common.network.RezolveMenuPacket;
import com.rezolvemc.common.registry.RegistryId;
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
