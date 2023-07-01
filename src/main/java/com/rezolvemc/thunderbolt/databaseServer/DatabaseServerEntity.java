package com.rezolvemc.thunderbolt.databaseServer;

import com.rezolvemc.common.blocks.BlockEntityBase;

import com.rezolvemc.common.registry.RezolveRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DatabaseServerEntity extends BlockEntityBase {
	public DatabaseServerEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(DatabaseServerEntity.class), pPos, pBlockState);
	}

	@Override
	protected void saveAdditional(CompoundTag pTag) {
		pTag.put("DB", this.db);
		super.saveAdditional(pTag);
	}
	
	private CompoundTag db = new CompoundTag();

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);

		if (pTag.contains("DB"))
			this.db = pTag.getCompound("DB").copy();
		else
			this.db = new CompoundTag();
	}

	private String getNameTag(ResourceKey<Level> level, BlockPos pos) {
		return String.format("%s|%d,%d,%d|name", level.location(), pos.getX(), pos.getY(), pos.getZ());
	}

	public void setMachineName(ResourceKey<Level> level, BlockPos pos, String name) {
		if (name == null || "".equals(name))
			this.db.remove(getNameTag(level, pos));
		else
			this.db.putString(getNameTag(level, pos), name);
		this.commit();
	}
	
	public String getMachineName(ResourceKey<Level> level, BlockPos pos) {
		return this.db.getString(getNameTag(level, pos));
	}
	
	public CompoundTag getDB() {
		return this.db;
	}
	
	public void commit() {
		this.setChanged();
	}

}
