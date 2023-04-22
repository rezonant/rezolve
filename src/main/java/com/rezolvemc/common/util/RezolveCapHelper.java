package com.rezolvemc.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.logging.Level;

public class RezolveCapHelper {
    public static IItemHandler getItemHandler(@Nullable BlockEntity entity, Direction direction) {
        if (entity == null)
            return null;
        return entity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).orElse(null);
    }
    public static IFluidHandler getFluidHandler(@Nullable BlockEntity entity, Direction direction) {
        if (entity == null)
            return null;
        return entity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).orElse(null);
    }
    public static IEnergyStorage getEnergyStorage(@Nullable BlockEntity entity, Direction direction) {
        if (entity == null)
            return null;
        return entity.getCapability(ForgeCapabilities.ENERGY, direction).orElse(null);
    }
}
