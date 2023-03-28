package com.astronautlabs.mc.rezolve.databaseServer;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.BlockEntityBase;

import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DatabaseServerEntity extends BlockEntityBase {
	public DatabaseServerEntity(BlockPos pPos, BlockState pBlockState) {
		super(RezolveRegistry.blockEntityType(DatabaseServerEntity.class), pPos, pBlockState);
	}

	@Override
	public CompoundTag serializeNBT() {
		var tag = super.serializeNBT();
		tag.put("DB", this.db);
		return tag;
	}
	
	private CompoundTag db = new CompoundTag();
	
	@Override
	public void deserializeNBT(CompoundTag compound) {
		super.deserializeNBT(compound);

		if (compound.contains("DB"))
			this.db = compound.getCompound("DB").copy();
		else 
			this.db = new CompoundTag();
	}
	
	public void setMachineName(BlockPos pos, String name) {
		String nameTag = "Name_"+pos.getX()+"_"+pos.getY()+"_"+pos.getZ();
		if (name == null || "".equals(name))
			this.db.remove(nameTag);
		else
			this.db.putString(nameTag, name);
		this.commit();
	}
	
	public String getMachineName(BlockPos pos) {
		return this.db.getString("Name_"+pos.getX()+"_"+pos.getY()+"_"+pos.getZ());	
	}
	
	public CompoundTag getDB() {
		return this.db;
	}
	
	public void commit() {
		this.setChanged();
	}

}
