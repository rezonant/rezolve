package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class CableEndpoint implements INBTSerializable<CompoundTag> {
    public CableEndpoint(BlockPos pos) {
        position = pos;
    }

    public CableEndpoint() {
    }

    private BlockPos position;
    private Map<BlockPos, BlockConfiguration> interfaces = new HashMap<>();

    public BlockPos getPosition() {
        return position;
    }

    public BlockConfiguration[] getInterfaces() {
        return interfaces.values().toArray(new BlockConfiguration[interfaces.size()]);
    }

    public void addInterface(BlockPos pos, BlockConfiguration configuration) {
        interfaces.put(pos, configuration);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("position", NbtUtils.writeBlockPos(position));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        position = NbtUtils.readBlockPos(nbt.getCompound("position"));
    }
}
