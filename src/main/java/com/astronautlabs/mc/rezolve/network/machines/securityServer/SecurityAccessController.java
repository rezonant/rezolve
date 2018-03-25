package com.astronautlabs.mc.rezolve.network.machines.securityServer;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.network.cable.CableNetwork;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SecurityAccessController {
	public SecurityAccessController() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void handleBlockRightClick(PlayerInteractEvent.RightClickBlock evt) {
		
		TileEntity entity = evt.getEntityPlayer().getEntityWorld().getTileEntity(evt.getPos());
		SecurityServerEntity securityServer;
		
		if (entity instanceof SecurityServerEntity) {
			securityServer = (SecurityServerEntity)entity;
			if (!securityServer.canPlayerOpen(evt.getEntityPlayer())) {
				evt.setCanceled(true);
			}
			
			return;
		}
		
		CableNetwork network = CableNetwork.networkAt(evt.getEntityPlayer().getEntityWorld(), evt.getPos(), RezolveMod.ETHERNET_CABLE_BLOCK);

		if (network != null) {
			securityServer = network.getSecurityServer();

			if (securityServer != null) {
				if (!securityServer.canPlayerUse(evt.getEntityPlayer(), evt.getPos())) {
					evt.setCanceled(true);
				}
			}
		}
	}
}
