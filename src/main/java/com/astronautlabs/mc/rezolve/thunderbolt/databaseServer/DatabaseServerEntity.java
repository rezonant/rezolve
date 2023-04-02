package com.astronautlabs.mc.rezolve.thunderbolt.databaseServer;

import com.astronautlabs.mc.rezolve.common.blocks.BlockEntityBase;

import com.astronautlabs.mc.rezolve.common.registry.RezolveRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
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
