package com.rezolvemc.thunderbolt.extender;

import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class ExtenderEntity extends BlockEntity {
    public ExtenderEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(ExtenderEntity.class), pPos, pBlockState);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        var direction = getBlockState().getValue(Extender.FACING);

        if (facing == direction) {
            return LazyOptional.empty();
        } else {
            var targetBlockPos = getBlockPos().relative(direction);
            var targetBlockEntity = getLevel().getBlockEntity(targetBlockPos);

            return targetBlockEntity.getCapability(capability, direction.getOpposite());
        }
    }
}
