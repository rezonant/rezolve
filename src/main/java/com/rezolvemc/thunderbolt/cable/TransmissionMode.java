package com.rezolvemc.thunderbolt.cable;

import net.minecraft.network.chat.Component;

import java.util.Arrays;
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

    public TransmissionMode seek(int direction) {
        if (direction > 0)
            return next();
        else if (direction < 0)
            return previous();
        else
            return this;
    }

    public TransmissionMode next() {
        var values = Arrays.asList(values());
        return values.get((values.indexOf(this) + 1) % values.size());
    }

    public TransmissionMode previous() {
        var values = Arrays.asList(values());
        var index = values.indexOf(this);
        return values.get(index > 0 ? index - 1 : values.size() - 1);
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
