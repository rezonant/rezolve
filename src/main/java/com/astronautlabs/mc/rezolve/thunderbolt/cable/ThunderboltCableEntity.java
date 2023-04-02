package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ThunderboltCableEntity extends MachineEntity {
    public ThunderboltCableEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(ThunderboltCableEntity.class), pPos, pBlockState);
    }
}
