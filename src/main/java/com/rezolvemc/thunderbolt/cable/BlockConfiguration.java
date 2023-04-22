package com.rezolvemc.thunderbolt.cable;

import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Thunderbolt configuration for a specific block
 */
public class BlockConfiguration implements INBTSerializable<CompoundTag> {
    public BlockConfiguration() {
        for (var dir : Direction.values()) {
            faces.add(new FaceConfiguration(dir));
        }
    }

    public BlockConfiguration(BlockPos pos) {
        this();
        position = pos;
    }

    private BlockPos position;
    private BlockState blockState;
    private List<FaceConfiguration> faces = new ArrayList<>();
    private boolean isCable;
    private boolean isInterface;

    public boolean isCable() {
        return isCable;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public BlockPos getPosition() {
        return position;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public List<FaceConfiguration> getFaces() {
        return faces;
    }

    public FaceConfiguration getFace(Direction dir) {
        var face = faces.stream().filter(f -> f.getDirection() == dir).findFirst().orElse(null);

        if (face == null) {
            face = new FaceConfiguration(dir);
            faces.add(face);
        }

        return face;
    }

    public void updateBlockState(Level level) {
        var blockState = level.getBlockState(position);
        var blockEntity = level.getBlockEntity(position);
        this.blockState = blockState;

        isCable = RezolveRegistry.block(ThunderboltCable.class).canConnectTo(level, position);
        isInterface = RezolveRegistry.block(ThunderboltCable.class).canInterfaceWith(level, position);

        for (var blockDir : Direction.values()) {
            FaceConfiguration cap = getFace(blockDir);
            cap.updateBlockState(blockState, blockEntity);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var list = new ListTag();

        for (var cap : faces)
            list.add(cap.serializeNBT());

        tag.put("position", NbtUtils.writeBlockPos(position));
        if (blockState != null)
            tag.putInt("blockState", Block.getId(blockState));
        tag.put("caps", list);

        tag.putBoolean("isInterface", isInterface);
        tag.putBoolean("isCable", isCable);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        position = NbtUtils.readBlockPos(nbt.getCompound("position"));

        // Block State

        if (nbt.contains("blockState")) {
            blockState = Block.stateById(nbt.getInt("blockState"));
        } else {
            blockState = null;
        }

        // Face Configurations

        faces.clear();
        if (nbt.contains("caps")) {
            var list = nbt.getList("caps", Tag.TAG_COMPOUND);
            for (var item : list) {
                var compound = (CompoundTag)item;
                var dir = Direction.byName(compound.getString("direction"));
                var cap = getFace(dir);
                cap.deserializeNBT((CompoundTag) item);
            }
        }

        // Flags

        isInterface = nbt.getBoolean("isInterface");
        isCable = nbt.getBoolean("isCable");
    }
}
