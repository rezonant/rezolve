package com.astronautlabs.mc.rezolve.common;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.bundleBuilder.BundleBuilderUpdateMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public abstract class GuiContainerBase extends GuiContainer {

	public GuiContainerBase(ContainerBase inventorySlotsIn, String guiBackgroundResource) {
		super(inventorySlotsIn);
		this.container = inventorySlotsIn;
		this.guiBackgroundResource = guiBackgroundResource;
	}
	
	public void initGui() {
		super.initGui();
		this.controls.clear();
	};
	
	ContainerBase container;
	String guiBackgroundResource;

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	    this.mc.getTextureManager().bindTexture(new ResourceLocation(this.guiBackgroundResource));
	    this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

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

		for (Gui control : this.controls) {
			if (control instanceof GuiTextField) {
				GuiTextField textField = (GuiTextField)control;
				textField.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		
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
	
	private ArrayList<Gui> controls = new ArrayList<Gui>();
	
	protected void addControl(Gui control) {
		this.controls.add(control);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
  
		boolean textFocused = false;
		
		for (Gui control : this.controls) {
			if (control instanceof GuiTextField) {
				GuiTextField textField = (GuiTextField) control;
				if (textField.isFocused()) {
					textFocused = true;
					textField.textboxKeyTyped(typedChar, keyCode);
					break;
				}
			}
		}

		if (!textFocused || keyCode != Keyboard.KEY_E)
			super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		for (Gui control : this.controls) {
			if (control instanceof GuiTextField) {
				GuiTextField textField = (GuiTextField)control;
				textField.updateCursorCounter();
			}
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

	    GlStateManager.pushMatrix();
    		GlStateManager.translate(-guiLeft, -guiTop, 0);
			for (Gui control : this.controls) {
				if (control instanceof GuiTextField) {
					((GuiTextField)control).drawTextBox();
				} else if (control instanceof GuiButton) {
					((GuiButton)control).drawButton(this.mc, mouseX, mouseY);
				}
			}
    	GlStateManager.popMatrix();		
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
}