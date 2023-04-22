package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.common.machines.MachineEntity;
import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class TesseractEntity extends MachineEntity {
    public TesseractEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(TesseractEntity.class), pPos, pBlockState);
    }

    @Override
    public Component getMenuTitle() {
        return Component.translatable("block.rezolve.tesseract");
    }
}
