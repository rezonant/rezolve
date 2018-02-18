package com.astronautlabs.mc.rezolve.bundler;

import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.common.Operation;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class BundlerGuiContainer extends GuiContainerBase {

	public BundlerGuiContainer(IInventory playerInv, BundlerEntity entity) {
		super(new BundlerContainer(playerInv, entity), "rezolve:textures/gui/container/bundler_gui.png", 255, 212);
		
		this.playerInv = playerInv;
		this.entity = entity;
	}
	
	private IInventory playerInv;
	private BundlerEntity entity;

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 231;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight - usedHeight, 0xFF000000);
	    
	    Operation<? extends TileEntity> op = this.entity.getCurrentOperation();
	    String statusStr;
	    
	    if (op != null) {
    		int width = (int)(32 * op.getPercentage() / (double)100);

    	    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    	    this.mc.getTextureManager().bindTexture(new ResourceLocation("rezolve:textures/gui/container/arrow.png"));
    	    GlStateManager.enableBlend();
    	    this.drawModalRectWithCustomSizedTexture(133, 54, 0, 0, width, 32, 32, 32);
    	    
    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }
	    
		this.fontRendererObj.drawString(statusStr, 7, 102, 0xFF000000);
	}
}
