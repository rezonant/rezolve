package com.astronautlabs.mc.rezolve.common.network;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(WithPackets.class)
public @interface WithPacket {
    Class<? extends RezolvePacket> value();
}
