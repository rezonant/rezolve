package com.rezolvemc.common;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class LevelPosition {
    public LevelPosition(ResourceKey<Level> level, BlockPos pos) {
        this.level = level;
        this.position = pos;
    }

    public LevelPosition(Level level, BlockPos pos) {
        this(level.dimension(), pos);
    }

    public static LevelPosition of(Level level, BlockPos pos) {
        return new LevelPosition(level, pos);
    }

    public static LevelPosition of(ResourceKey<Level> level, BlockPos pos) {
        return new LevelPosition(level, pos);
    }

    private final ResourceKey<Level> level;
    private final BlockPos position;

    public ResourceKey<Level> getLevelKey() {
        return level;
    }

    public BlockPos getPosition() {
        return position;
    }

    public boolean is(BlockPos pos) {
        return Objects.equals(position, pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof LevelPosition other) {
            return level == other.level && Objects.equals(position, other.position);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level.location().toString(), position);
    }
}
