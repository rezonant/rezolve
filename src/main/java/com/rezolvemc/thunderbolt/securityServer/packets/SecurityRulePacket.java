package com.rezolvemc.thunderbolt.securityServer.packets;

import com.rezolvemc.common.network.RezolveMenuPacket;
import com.rezolvemc.thunderbolt.securityServer.SecurityRule;
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
