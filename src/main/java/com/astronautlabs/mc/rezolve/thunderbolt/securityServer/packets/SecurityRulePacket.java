package com.astronautlabs.mc.rezolve.thunderbolt.securityServer.packets;

import com.astronautlabs.mc.rezolve.common.gui.RezolveMenuPacket;
import com.astronautlabs.mc.rezolve.thunderbolt.securityServer.SecurityRule;
import net.minecraft.network.FriendlyByteBuf;

public class SecurityRulePacket extends RezolveMenuPacket {
    public SecurityRule rule;

    @Override
    public void read(FriendlyByteBuf buf) {
        super.read(buf);
        rule = new SecurityRule();
        rule.deserializeNBT(buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        super.write(buf);
        buf.writeNbt(rule.serializeNBT());
    }
}
