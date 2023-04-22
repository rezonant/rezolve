package com.rezolvemc.common.network;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface WithPackets {
    WithPacket[] value();
}
