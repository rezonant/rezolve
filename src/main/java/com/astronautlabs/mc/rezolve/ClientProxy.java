package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.common.ITooltipHint;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber
public class ClientProxy extends CommonProxy {
	public ClientProxy() {
		super();
	}
	
	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		
		MinecraftForge.EVENT_BUS.register(this); 
	
		this.log("Initializing client-side proxy...");
		//this.mod.registerBlockRenderers();
		//this.mod.registerItemRenderers();
	}
	
	@SubscribeEvent
	public void handleTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		
		if (item instanceof BlockItem) {
			BlockItem ib = (BlockItem) item;
			Block block = ib.getBlock();
			
			if (block instanceof ITooltipHint) {
				String hint = ((ITooltipHint)block).getTooltipHint(stack);
				String[] strs = hint.split("\n");
				
				for (String line : strs)
					event.getToolTip().add(Component.literal(line));
			}
			
			return;
		}
		
		if (item instanceof ITooltipHint) {
			String hint = ((ITooltipHint)item).getTooltipHint(stack);
			
			String[] strs = hint.split("\n");
			
			for (String line : strs)
				event.getToolTip().add(Component.literal(line));
		}
	}
	
}
