package com.rezolvemc.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class LevelBlockFace extends LevelPosition {
    public LevelBlockFace(ResourceKey<Level> level, BlockPos pos, Direction face) {
        super(level, pos);

        this.face = face;
    }

    public LevelBlockFace(Level level, BlockPos pos, Direction face) {
        super(level, pos);

        this.face = face;
    }

    public final Direction face;
}
