package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.common.ContainerBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class RemoteShellContainer extends ContainerBase<RemoteShellEntity> {

	public RemoteShellContainer(IInventory playerInv, RemoteShellEntity entity) {
		super(entity);
	}

	@Override
	public boolean canInteractWith(EntityPlayer arg0) {
		return true;
	}

}
