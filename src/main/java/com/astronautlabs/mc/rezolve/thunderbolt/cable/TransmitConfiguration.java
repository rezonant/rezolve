package com.astronautlabs.mc.rezolve.thunderbolt.cable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public class TransmitConfiguration implements INBTSerializable<CompoundTag> {
    public TransmitConfiguration() {

    }

    public TransmitConfiguration(TransmissionType type) {
        this.type = type;
        if (type == TransmissionType.ENERGY)
            this.mode = TransmissionMode.AUTO;
    }

    private TransmissionType type = TransmissionType.ITEMS;
    private TransmissionMode mode = TransmissionMode.NONE;
    private boolean supported = false;

    public void setType(@Nonnull TransmissionType type) {
        this.type = type;
    }

    public void setMode(@Nonnull TransmissionMode mode) {
        this.mode = mode;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean value) {
        supported = value;
    }

    public TransmissionType getType() {
        return type;
    }

    public TransmissionMode getMode() {
        return mode;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();

        tag.putString("type", type.getKey());
        tag.putString("mode", mode.getKey());
        tag.putBoolean("supported", supported);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        type = TransmissionType.byKey(nbt.getString("type"));
        mode = TransmissionMode.byKey(nbt.getString("mode"));
        supported = nbt.getBoolean("supported");
    }
}
