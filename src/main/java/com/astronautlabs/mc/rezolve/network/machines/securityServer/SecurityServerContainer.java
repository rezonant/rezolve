package com.astronautlabs.mc.rezolve.network.machines.securityServer;

import com.astronautlabs.mc.rezolve.common.ContainerBase;

import net.minecraft.entity.player.EntityPlayer;

public class SecurityServerContainer extends ContainerBase<SecurityServerEntity> {

	public SecurityServerContainer(EntityPlayer player, SecurityServerEntity entity) {
		super(entity);
	}
}
