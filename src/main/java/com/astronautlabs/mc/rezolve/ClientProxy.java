package com.astronautlabs.mc.rezolve;

import com.astronautlabs.mc.rezolve.common.BlockBase;
import com.astronautlabs.mc.rezolve.common.ITooltipHint;

import com.astronautlabs.mc.rezolve.common.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {
	public ClientProxy() {
		super();
	}

	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		
		MinecraftForge.EVENT_BUS.register(this); 
	
		this.log("Initializing client-side proxy...");
		this.registerBlockRenderers();
		this.registerItemRenderers();
		this.registerEntityRenderers();
	}

	public void registerBlockRenderers() {
		this.log("Registering block renderers...");
		for (BlockBase block : this.mod.getRegisteredBlocks()) {
			this.log("Registering renderer for: " + block.getRegistryName());
			block.registerRenderer();
		}
	}

	public void registerItemRenderers() {
		this.log("Registering item renderers...");
		for (ItemBase item : this.mod.getRegisteredItems()) {
			this.log("Registering renderer for: " + item.getRegistryName());
			item.registerRenderer();
		}
	}

	public void registerEntityRenderers() {
		this.log("Registering entity renderers...");
		for (RezolveMod.EntityRegistration entity : this.mod.getRegisteredEntities()) {
			this.log("Registering renderer for: " + entity.entityClass.toString());
			RenderingRegistry.registerEntityRenderingHandler(entity.entityClass, entity.renderFactory);
		}
	}

	@SubscribeEvent
	public void handleTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		
		if (item instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock)item;
			Block block = ib.getBlock();
			
			if (block instanceof ITooltipHint) {
				String hint = ((ITooltipHint)block).getTooltipHint(stack);
				String[] strs = hint.split("\n");
				
				for (String line : strs)
					event.getToolTip().add(line);
			}
			
			return;
		}
		
		if (item instanceof ITooltipHint) {
			String hint = ((ITooltipHint)item).getTooltipHint(stack);
			
			String[] strs = hint.split("\n");
			
			for (String line : strs)
				event.getToolTip().add(line);
		}
	}
	
}
