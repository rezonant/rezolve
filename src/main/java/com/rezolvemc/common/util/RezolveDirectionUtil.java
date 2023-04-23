package com.rezolvemc.common.util;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class RezolveDirectionUtil {
    public static Component friendly(Direction direction) {
        return Component.translatable("rezolve.directions." + direction.getName());
    }
}
