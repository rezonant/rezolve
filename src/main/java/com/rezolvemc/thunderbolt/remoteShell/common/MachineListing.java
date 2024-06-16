package com.rezolvemc.thunderbolt.remoteShell.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public class MachineListing implements INBTSerializable<CompoundTag> {
    public MachineListing() {
    }

    public MachineListing(ResourceKey<Level> level, BlockPos pos, String name, ItemStack item) {
        this.level = level;
        this.blockPos = pos;
        this.name = name;
        this.item = item;
    }

    private ResourceKey<Level> level;
    private BlockPos blockPos;
    private String name;
    private ItemStack item;

    public ResourceKey<Level> getLevel() {
        return level;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString("level", level.location().toString());
        tag.put("blockPos", NbtUtils.writeBlockPos(blockPos));
        if (name != null)
            tag.putString("name", name);
        tag.put("item", item.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("level")) {
            level = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("level")));
        } else {
            level = Level.OVERWORLD;
        }

        blockPos = NbtUtils.readBlockPos(nbt.getCompound("blockPos"));
        if (nbt.contains("name"))
            name = nbt.getString("name");
        else
            name = null;
        item = ItemStack.of(nbt.getCompound("item"));
    }

    public static MachineListing of(CompoundTag tag) {
        var listing = new MachineListing();
        listing.deserializeNBT(tag);
        return listing;
    }
}
