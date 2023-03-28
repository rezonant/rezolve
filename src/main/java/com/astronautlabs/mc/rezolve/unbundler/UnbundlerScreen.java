package com.astronautlabs.mc.rezolve.unbundler;

import com.astronautlabs.mc.rezolve.bundler.BundlerEntity;
import com.astronautlabs.mc.rezolve.common.BaseScreen;
import com.astronautlabs.mc.rezolve.common.Operation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class UnbundlerScreen extends BaseScreen {

	public UnbundlerScreen(UnbundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/unbundler_gui.png", 255, 212);
	}

	@Override
	public void render(PoseStack pPoseStack, int mouseX, int mouseY, float partialTick) {
		super.render(pPoseStack, mouseX, mouseY, partialTick);

	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 226;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = 0; // TODO rfBarHeight - (int)(this.entity.getEnergyStored(Direction.DOWN) / (double)this.entity.getMaxEnergyStored(Direction.DOWN) * rfBarHeight);
	    colorQuad(0xFF000000, rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + usedHeight);

	    // Draw operation details
//
//	    Operation<BundlerEntity> op = this.entity.getCurrentOperation();
//	    String statusStr;
//
//	    if (op != null) {
//    		int width = (int)(32 * op.getPercentage() / (double)100);
//
//    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//			RenderSystem.enableBlend();
//
//			//textureQuad(new ResourceLocation("rezolve:textures/gui/container/arrow.png"), 103, 81, 0, 0, width, 32);
//
//    		statusStr = "Operation: "+op.getPercentage()+"%";
//	    } else {
//	    	statusStr = "Idle.";
//	    }
	    
		//this.font.draw(pPoseStack, statusStr, 7, 112, 0xFF000000);
	}
}
