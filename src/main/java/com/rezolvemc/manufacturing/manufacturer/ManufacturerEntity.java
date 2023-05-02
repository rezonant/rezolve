package com.rezolvemc.manufacturing.manufacturer;

import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ManufacturerEntity extends MachineEntity {
    public ManufacturerEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(ManufacturerEntity.class), pPos, pBlockState);
    }
}
