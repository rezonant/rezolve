package com.astronautlabs.mc.rezolve.machines.unbundler;

import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.common.Operation;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class UnbundlerGuiContainer extends GuiContainerBase {

	public UnbundlerGuiContainer(IInventory playerInv, UnbundlerEntity entity) {
		super(new UnbundlerContainer(playerInv, entity), "rezolve:textures/gui/container/unbundler_gui.png", 255, 212);
		
		this.playerInv = playerInv;
		this.entity = entity;
	}
	
	private IInventory playerInv;
	private UnbundlerEntity entity;

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 226;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = rfBarHeight - (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + usedHeight, 0xFF000000);

	    // Draw operation details
	    
	    Operation<? extends TileEntity> op = this.entity.getCurrentOperation();
	    String statusStr;
	    
	    if (op != null) {
    		int width = (int)(32 * op.getPercentage() / (double)100);

    	    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    	    this.mc.getTextureManager().bindTexture(new ResourceLocation("rezolve:textures/gui/container/arrow.png"));
    	    GlStateManager.enableBlend();
    	    this.drawModalRectWithCustomSizedTexture(103, 81, 0, 0, width, 32, 32, 32);
    	    
    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }
	    
		this.fontRendererObj.drawString(statusStr, 7, 112, 0xFF000000);
	}
}
