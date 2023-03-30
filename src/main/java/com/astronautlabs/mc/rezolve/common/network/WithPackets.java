package com.astronautlabs.mc.rezolve.common.network;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface WithPackets {
    WithPacket[] value();
}
