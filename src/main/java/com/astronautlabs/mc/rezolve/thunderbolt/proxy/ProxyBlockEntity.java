package com.astronautlabs.mc.rezolve.thunderbolt.proxy;

import com.astronautlabs.mc.rezolve.common.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.thunderbolt.cable.ThunderboltCableEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Arrays;
import java.util.Objects;

public class ProxyBlockEntity extends MachineEntity {
    public ProxyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RezolveRegistry.blockEntityType(ProxyBlockEntity.class), pPos, pBlockState);
    }

    public BlockEntity getConnectedEntity() {
        for (var dir : Direction.values()) {
            var entity = level.getBlockEntity(getBlockPos().relative(dir));
            if (entity == null)
                continue;
            if (entity instanceof ThunderboltCableEntity)
                continue;
            return entity;
        }

        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        var network = getNetwork();
        if (network == null)
            return LazyOptional.empty();

        var otherProxyEndpoint = Arrays.stream(network.getEndpointsByEntity(getClass()))
                .filter(e -> !e.is(getBlockPos()))
                .findFirst()
                .orElse(null)
                ;

        if (otherProxyEndpoint == null)
            return LazyOptional.empty();

        var otherProxy = otherProxyEndpoint.getBlockEntity(ProxyBlockEntity.class);

        if (otherProxy == null)
            return LazyOptional.empty();

        var remoteEntity = otherProxy.getConnectedEntity();

        if (remoteEntity == null)
            return LazyOptional.empty();

        return remoteEntity.getCapability(capability, facing);
    }
}
