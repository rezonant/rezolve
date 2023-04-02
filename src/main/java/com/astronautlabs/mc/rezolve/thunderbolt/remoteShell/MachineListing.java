package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.common.util.RezolveTagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class MachineListing implements INBTSerializable<CompoundTag> {
    public MachineListing() {
    }

    public MachineListing(BlockPos pos, String name, ItemStack item) {
        this.blockPos = pos;
        this.name = name;
        this.item = item;
    }

    BlockPos blockPos;
    String name;
    ItemStack item;

    BlockPos getBlockPos() {
        return blockPos;
    }

    String getName() {
        return name;
    }

    ItemStack getItem() {
        return item;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("blockPos", NbtUtils.writeBlockPos(blockPos));
        tag.putString("name", name);
        tag.put("item", item.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        blockPos = NbtUtils.readBlockPos(nbt.getCompound("blockPos"));
        name = nbt.getString("name");
        item = ItemStack.of(nbt.getCompound("item"));
    }

    public static MachineListing of(CompoundTag tag) {
        var listing = new MachineListing();
        listing.deserializeNBT(tag);
        return listing;
    }
}
