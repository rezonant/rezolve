package com.astronautlabs.mc.rezolve.machines.databaseServer;

import com.astronautlabs.mc.rezolve.common.TileEntityBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class DatabaseServerEntity extends TileEntityBase {

	public DatabaseServerEntity() {
		super("database_server_tile_entity");
		this.db = new NBTTagCompound();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("DB", this.db);
		return super.writeToNBT(compound);
	}
	
	private NBTTagCompound db = null;
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("DB"))
			this.db = compound.getCompoundTag("DB").copy();
		else 
			this.db = new NBTTagCompound();
		
		super.readFromNBT(compound);
	}
	
	public void setMachineName(BlockPos pos, String name) {
		String nameTag = "Name_"+pos.getX()+"_"+pos.getY()+"_"+pos.getZ();
		if (name == null || "".equals(name))
			this.db.removeTag(nameTag);
		else
			this.db.setString(nameTag, name);
		this.commit();
	}
	
	public String getMachineName(BlockPos pos) {
		return this.db.getString("Name_"+pos.getX()+"_"+pos.getY()+"_"+pos.getZ());	
	}
	
	public NBTTagCompound getDB() {
		return this.db;
	}
	
	public void commit() {
		this.notifyUpdate();
	}

}
