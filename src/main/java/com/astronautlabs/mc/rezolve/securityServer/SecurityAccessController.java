package com.astronautlabs.mc.rezolve.securityServer;

import com.astronautlabs.mc.rezolve.BundleItem;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.registry.RezolveRegistry;
import com.astronautlabs.mc.rezolve.remoteShell.CableNetwork;

import com.astronautlabs.mc.rezolve.remoteShell.EthernetCableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SecurityAccessController {
	public SecurityAccessController() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void handleBlockRightClick(PlayerInteractEvent.RightClickBlock evt) {
		
		BlockEntity entity = evt.getEntity().level.getBlockEntity(evt.getPos());
		SecurityServerEntity securityServer;
		
		if (entity instanceof SecurityServerEntity) {
			securityServer = (SecurityServerEntity)entity;
			if (!securityServer.canPlayerOpen(evt.getEntity())) {
				evt.setCanceled(true);
			}
			
			return;
		}
		
		CableNetwork network = new CableNetwork(evt.getEntity().level, evt.getPos(), RezolveRegistry.block(EthernetCableBlock.class));
		securityServer = network.getSecurityServer();
		
		if (securityServer != null) {
			if (!securityServer.canPlayerUse(evt.getEntity(), evt.getPos())) {
				evt.setCanceled(true);
			}
		}
	}
}
