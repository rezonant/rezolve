package com.rezolvemc.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class LevelPosition {
    public LevelPosition(ResourceKey<Level> levelKey, BlockPos blockPos) {
        this.levelKey = levelKey;
        this.blockPos = blockPos;
    }

    public LevelPosition(Level levelKey, BlockPos blockPos) {
        this(levelKey.dimension(), blockPos);
    }

    public static LevelPosition of(Level level, BlockPos blockPos) {
        return new LevelPosition(level, blockPos);
    }

    public static LevelPosition of(ResourceKey<Level> level, BlockPos blockPos) {
        return new LevelPosition(level, blockPos);
    }

    public final ResourceKey<Level> levelKey;
    public final BlockPos blockPos;

    public boolean is(BlockPos pos) {
        return Objects.equals(blockPos, pos);
    }

    public LevelBlockFace withFace() {
        return withFace(null);
    }

    public LevelBlockFace withFace(Direction face) {
        return new LevelBlockFace(levelKey, blockPos, face);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof LevelPosition other) {
            return levelKey == other.levelKey && Objects.equals(blockPos, other.blockPos);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelKey.location().toString(), blockPos);
    }
}
