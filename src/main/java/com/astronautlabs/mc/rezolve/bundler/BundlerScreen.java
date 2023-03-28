package com.astronautlabs.mc.rezolve.bundler;

import com.astronautlabs.mc.rezolve.common.BaseScreen;
import com.astronautlabs.mc.rezolve.common.Operation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BundlerScreen extends BaseScreen {

	public BundlerScreen(BundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/bundler_gui.png", 255, 212);

		this.playerInv = playerInv;
		this.entity = entity;
	}
	
	private Inventory playerInv;
	private BundlerEntity entity;

	@Override
	public void render(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
		super.render(pPoseStack, mouseX, mouseY, pPartialTick);

	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 231;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = 0; // TODO (int)(this.entity.getEnergyStored(Direction.DOWN) / (double)this.entity.getMaxEnergyStored(Direction.DOWN) * rfBarHeight);

		colorQuad(1, 0, 0, 1, rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight - usedHeight);
	    
	    Operation<BundlerEntity> op = this.entity.getCurrentOperation();
	    String statusStr;
	    
	    if (op != null) {
    		int width = (int)(32 * op.getPercentage() / (double)100);

    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			RenderSystem.enableBlend();
			textureQuad(new ResourceLocation("rezolve:textures/gui/container/arrow.png"), 133, 54, width, 32);
    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }
	    
		this.font.draw(pPoseStack, statusStr, 7, 102, 0xFF000000);
	}
}
