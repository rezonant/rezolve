package com.astronautlabs.mc.rezolve.securityServer;

import com.astronautlabs.mc.rezolve.common.ContainerBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class SecurityServerContainer extends ContainerBase {

	public SecurityServerContainer(EntityPlayer player, SecurityServerEntity entity) {
		super(entity);
	}
}
