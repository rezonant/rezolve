package com.astronautlabs.mc.rezolve.thunderbolt.securityServer;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.UUID;

public class SecurityRule implements INBTSerializable<CompoundTag> {
    public SecurityRule() {

    }

    public SecurityRule(String id, String name, int mode) {
        this.id = id;
        this.name = name;
        this.mode = mode;
    }

    public SecurityRule(String name, int mode) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.mode = mode;
    }

    /**
     * Machine mode: Machines default to open, only stopping players with RESTRICTED policy.
     */
    public static final int MODE_OPEN = -1;

    /**
     * Machine mode: Machines default to closed, only allowing players with ALLOWED or OWNER policy
     * Player mode: Negative policy
     */
    public static final int MODE_RESTRICTED = 0;

    /**
     *
     */
    public static final int MODE_ALLOWED = 1;
    public static final int MODE_OWNER = 2;

    public static final int MODE_NONE = 0;
    public static final int MODE_PROTECTED = 1;

    String id;
    String name;
    int mode;

    public boolean draft = false;

    public String getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

    public SecurityRule copy() {
        return new SecurityRule(this.id, this.name, this.mode);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (name != null)
            tag.putString("name", name);
        if (id != null)
            tag.putString("id", id);
        tag.putInt("mode", mode);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("name"))
            name = nbt.getString("name");
        else
            name = null;

        if (nbt.contains("id"))
            id = nbt.getString("id");
        else
            id = null;

        mode = nbt.getInt("mode");
    }
}
