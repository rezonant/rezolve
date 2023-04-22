package com.rezolvemc.thunderbolt.cable;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public enum TransmissionType {
    ITEMS("items"),
    FLUIDS("fluids"),
    ENERGY("energy"),
    ;

    TransmissionType(String key) {
        this.key = key;
    }

    private String key;

    public String getKey() {
        return key;
    }

    public static TransmissionType byKey(String key) {
        for (var value : values()) {
            if (Objects.equals(value.getKey(), key))
                return value;
        }

        return null;
    }

    public Component translation() {
        return Component.translatable("rezolve.thunderbolt.transmission_types." + key);
    }
}
