package com.astronautlabs.mc.rezolve.common;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBase extends TileEntity  {
	TileEntityBase(String registryName) {
		this.registryName = registryName;
	}
	
	private String registryName;
	
	public String getRegistryName() {
		return this.registryName;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

	    if (this.customName != null) {
	        compound.setString("CustomName", this.getCustomName());
	    }
	    
		if (this instanceof IInventory) {
			BundlerNBT.writeInventory(compound, (IInventory)this);
		}
		
		if (this.storedEnergy >= 0)
			compound.setInteger("RF", this.storedEnergy);
		
		return super.writeToNBT(compound);
	}
	
	protected int storedEnergy = -1;
	
	protected void notifyUpdate() {
		IBlockState state = this.worldObj.getBlockState(pos);
		this.worldObj.notifyBlockUpdate(this.pos, state, state, 3);
	}

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(pos, 1, tagCompound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    		this.readFromNBT(pkt.getNbtCompound());
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
    		NBTTagCompound tagCompound = super.getUpdateTag();
		this.writeToNBT(tagCompound);
		return tagCompound;
    }
    
	@Override
	public void readFromNBT(NBTTagCompound compound) {

	    if (compound.hasKey("CustomName", 8)) {
	        this.setCustomName(compound.getString("CustomName"));
	    }
	    
	    if (compound.hasKey("RF"))
	    	this.storedEnergy = compound.getInteger("RF");
	    
		if (this instanceof IInventory) {
			BundlerNBT.readInventory(compound, (IInventory)this);
		}
		
		super.readFromNBT(compound);
	}

    private String customName;
    
    public String getCustomName() {
        return this.customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }    
}
