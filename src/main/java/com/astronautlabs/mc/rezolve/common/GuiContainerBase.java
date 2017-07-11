package com.astronautlabs.mc.rezolve.common;

import java.io.IOException;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.bundleBuilder.BundleBuilderUpdateMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class GuiContainerBase extends GuiContainer {

	public GuiContainerBase(ContainerBase inventorySlotsIn) {
		super(inventorySlotsIn);
		this.container = inventorySlotsIn;
	}
	
	ContainerBase container;
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		Slot slot = this.getSlotUnderMouse();
		
		if (slot != null) {
			if (slot instanceof GhostSlot) {
				return;
			}
		}
		
		// TODO Auto-generated method stub
		super.mouseReleased(mouseX, mouseY, state);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		Slot slot = this.getSlotUnderMouse();
		
		if (slot != null) {
			if (slot instanceof GhostSlot) {
				GhostSlot ghostSlot = (GhostSlot)slot;
				EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
				ItemStack stack = player.inventory.getItemStack();
				
				if (stack != null) {
					stack = stack.copy(); 
					
					if (ghostSlot.isSingleItemOnly())
						stack.stackSize = 1;
				}
				
				IInventory inventory = (IInventory)this.container.getEntity();
				inventory.setInventorySlotContents(slot.getSlotIndex(), stack);
				
				if (Minecraft.getMinecraft().theWorld.isRemote) {
					// Send to the server
					RezolvePacketHandler.INSTANCE.sendToServer(new GhostSlotUpdateMessage(player, this.container.getEntity(), slot.getSlotIndex(), stack));
				}
				
				return;
			}
		}
		
		// TODO Auto-generated method stub
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}