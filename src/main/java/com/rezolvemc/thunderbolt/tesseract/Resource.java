package com.rezolvemc.thunderbolt.tesseract;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Resource implements ICapabilityProvider {
    Resource(@NotNull Level level, @NotNull BlockPos block, @NotNull Direction face) {
        this.level = level;
        this.block = block;
        this.face = face;
    }

    @NotNull public final Level level;
    @NotNull public final BlockPos block;
    @NotNull public final Direction face;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        var entity = level.getExistingBlockEntity(block);
        if (entity == null)
            return LazyOptional.empty();

        return entity.getCapability(cap, face);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return level.equals(resource.level) && block.equals(resource.block) && face == resource.face;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, block, face);
    }
}
