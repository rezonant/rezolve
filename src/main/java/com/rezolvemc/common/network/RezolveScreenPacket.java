package com.rezolvemc.common.network;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.MachineScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

public abstract class RezolveScreenPacket extends RezolvePacket {
    private static final Logger LOGGER = Rezolve.logger(RezolveScreenPacket.class);

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void receiveOnClient() {
        var screen = Minecraft.getInstance().screen;

        if (screen instanceof MachineScreen<?> machineScreen) {
            machineScreen.receivePacket(this);
        } else if (screen != null) {
            LOGGER.error("Received packet {} but current screen ({}) does not extend from MachineScreen.", this.getClass().getCanonicalName(), screen.getClass().getCanonicalName());
        } else {
            LOGGER.error("Received packet {} but there is no active screen to receive it");
        }
    }
}
