package com.astronautlabs.mc.rezolve.bundles.bundler;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.astronautlabs.mc.rezolve.common.machines.Operation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BundlerScreen extends MachineScreen<BundlerMenu> {

	public BundlerScreen(BundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/bundler_gui.png", 255, 212);

		this.playerInv = playerInv;
		this.entity = entity;

		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 49;
		this.inventoryLabelY = 113;
	}
	
	private Inventory playerInv;
	private BundlerEntity entity;

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
		super.renderContents(pPoseStack, mouseX, mouseY, pPartialTick);

	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 231;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    double usedHeight = this.menu.energyStored / (double)this.menu.energyCapacity * rfBarHeight;
		colorQuad(pPoseStack, 0, 0, 0, 1, rfBarX, rfBarY, rfBarWidth, rfBarHeight - usedHeight);

	    Operation<BundlerEntity> op = this.menu.currentOperation;
	    String statusStr;
	    
	    if (op != null) {
    		int width = (int)(32 * op.getPercentage() / (double)100);

    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			RenderSystem.enableBlend();
			textureQuad(pPoseStack, new ResourceLocation("rezolve:textures/gui/container/arrow.png"), 133, 54, width, 32);
    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		this.font.draw(pPoseStack, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666);
	}
}
