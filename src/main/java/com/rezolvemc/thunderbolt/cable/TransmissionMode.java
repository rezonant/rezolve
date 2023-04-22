package com.rezolvemc.thunderbolt.cable;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public enum TransmissionMode {
    NONE("none"),
    PULL("pull"),
    PUSH("push"),
    AUTO("auto"),
    ;

    TransmissionMode(String name) {
        this.key = name;
    }

    private final String key;

    public String getKey() {
        return key;
    }

    public boolean canPull() {
        return this == PULL || this == AUTO;
    }

    public boolean canPush() {
        return this == PUSH || this == AUTO;
    }

    public static TransmissionMode byKey(String key) {
        for (var value : values()) {
            if (Objects.equals(value.getKey(), key))
                return value;
        }

        return null;
    }

    public Component translation() {
        return Component.translatable("rezolve.thunderbolt.transmission_modes." + key);
    }
}
