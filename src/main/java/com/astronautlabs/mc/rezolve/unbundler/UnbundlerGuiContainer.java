package com.astronautlabs.mc.rezolve.unbundler;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class UnbundlerGuiContainer extends GuiContainer {

	public UnbundlerGuiContainer(IInventory playerInv, UnbundlerEntity entity) {
		super(new UnbundlerContainer(playerInv, entity));
		
		this.playerInv = playerInv;
		this.entity = entity;
	    this.xSize = 255;
	    this.ySize = 212;
	}
	
	private IInventory playerInv;
	private UnbundlerEntity entity;
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	    this.mc.getTextureManager().bindTexture(new ResourceLocation("rezolve:textures/gui/container/bundler_gui.png"));
	    this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	    
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 231;
	    int rfBarY = 25;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight, 0xFF000000);
	    Gui.drawRect(rfBarX, rfBarY + rfBarHeight - usedHeight, rfBarX + rfBarWidth, rfBarY + rfBarHeight, 0xFFFF0000);
	    
	}
}
