package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Thunderbolt configuration for a given face of a block
 */
public class FaceConfiguration implements INBTSerializable<CompoundTag> {
    public FaceConfiguration() {
        this(Direction.NORTH);
    }

    public FaceConfiguration(Direction direction) {
        this.direction = direction;
    }

    private Direction direction;
    private List<TransmitConfiguration> transmitConfigurations = new ArrayList<>();

    public List<TransmitConfiguration> getTransmissionConfigurations() {
        return transmitConfigurations;
    }

    public TransmitConfiguration getTransmissionConfiguration(TransmissionType type) {
        var config = transmitConfigurations.stream().filter(tc -> tc.getType() == type).findFirst().orElse(null);
        if (config == null) {
            config = new TransmitConfiguration(type);
            transmitConfigurations.add(config);
        }

        return config;
    }

    public boolean supportsEnergy() {
        return getTransmissionConfiguration(TransmissionType.ENERGY).isSupported();
    }

    public boolean supportsItems() {
        return getTransmissionConfiguration(TransmissionType.ITEMS).isSupported();
    }

    public boolean supportsFluids() {
        return getTransmissionConfiguration(TransmissionType.FLUIDS).isSupported();
    }

    public void setSupported(TransmissionType type, boolean isSupported) {
        getTransmissionConfiguration(type).setSupported(isSupported);
    }
    public void updateBlockState(BlockState blockState, BlockEntity blockEntity) {
        if (blockEntity != null) {
            setSupported(TransmissionType.ITEMS, blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).isPresent());
            setSupported(TransmissionType.ENERGY, blockEntity.getCapability(ForgeCapabilities.ENERGY, direction).isPresent());
            setSupported(TransmissionType.FLUIDS, blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).isPresent());
        } else {
            setSupported(TransmissionType.ITEMS, false);
            setSupported(TransmissionType.ENERGY, false);
            setSupported(TransmissionType.FLUIDS, false);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("direction", direction.getName());

        var list = new ListTag();
        for (var transmitConfig : transmitConfigurations) {
            list.add(transmitConfig.serializeNBT());
        }

        tag.put("transmitConfigurations", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        direction = nbt.contains("direction") ? Direction.byName(nbt.getString("direction")) : null;
        var list = nbt.getList("transmitConfigurations", Tag.TAG_COMPOUND);

        for (var tag : list) {
            var configTag = (CompoundTag) tag;
            var type = TransmissionType.byKey(configTag.getString("type"));
            var config = getTransmissionConfiguration(type);
            config.deserializeNBT(configTag);
        }
    }

    public Direction getDirection() {
        return direction;
    }
}
