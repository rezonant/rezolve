package com.astronautlabs.mc.rezolve.bundles.unbundler;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;

import com.astronautlabs.mc.rezolve.common.machines.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class UnbundlerScreen extends MachineScreen<UnbundlerMenu> {

	public UnbundlerScreen(UnbundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/unbundler_gui.png", 255, 212);

		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 49;
		this.inventoryLabelY = 113;
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float partialTick) {
		super.renderContents(pPoseStack, mouseX, mouseY, partialTick);

	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 226;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;

		double usedHeight = menu.energyStored / (double)menu.energyCapacity * rfBarHeight;
		colorQuad(pPoseStack, 0, 0, 0, 1, rfBarX, rfBarY, rfBarWidth, rfBarHeight - usedHeight);

	    // Draw operation details

	    Operation<UnbundlerEntity> op = this.menu.operation;
	    String statusStr;

	    if (op != null) {
			float progress = menu.progress;
			int width = (int)(32 * progress);

    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();

			textureQuad(pPoseStack, new ResourceLocation("rezolve:textures/gui/container/arrow.png"), 103, 81, width, 32, 0, 0, progress, 1);

    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		this.font.draw(pPoseStack, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666);
	}
}
