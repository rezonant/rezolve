package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.Rezolve;

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
		
		BlockEntity entity = evt.getEntity().level().getBlockEntity(evt.getPos());
		SecurityServerEntity securityServer;
		
		if (entity instanceof SecurityServerEntity) {
			securityServer = (SecurityServerEntity)entity;
			if (!securityServer.canPlayerOpen(evt.getEntity())) {
				evt.setCanceled(true);
			}
			
			return;
		}
		
		securityServer = Rezolve.getGoverningSecurityServer(evt.getEntity().level(), evt.getPos());
		
		if (securityServer != null) {
			if (!securityServer.canPlayerUse(evt.getEntity(), evt.getPos())) {
				evt.setCanceled(true);
			}
		}
	}
}
