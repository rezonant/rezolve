package com.rezolvemc.bundles.bundler;

import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.machines.Operation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BundlerScreen extends MachineScreen<BundlerMenu> {

	public BundlerScreen(BundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 255, 212);

		this.inventoryLabelX = 49;
		this.inventoryLabelY = 113;
		this.twoToneHeight = 125;
	}

	@Override
	protected void setup() {
		super.setup();

		addEnergyMeter(leftPos + 231, topPos + 20, 88);
		addOperationProgressIndicator(leftPos + 133, topPos + 54);

		addSlotGrid(
				Component.translatable("screens.rezolve.input_items"),
				3, 0, 9
		);

		addSlotGrid(
				Component.translatable("screens.rezolve.patterns"),
				3, 9, 9
		);

		addSlotGrid(
				Component.translatable("screens.rezolve.bundles"),
				3, 18, 9
		);
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
		super.renderContents(pPoseStack, mouseX, mouseY, pPartialTick);

	    Operation<BundlerEntity> op = this.menu.operation;
	    String statusStr;
	    
	    if (op != null) {
			float progress = menu.progress;
    		int width = (int)(32 * progress);

    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			textureQuad(pPoseStack, new ResourceLocation("rezolve:textures/gui/container/arrow.png"), 133, 54, width, 32, 0, 0, (float)progress, 1);
    		statusStr = "Operation: "+(Math.floor(progress * 100))+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		this.font.draw(pPoseStack, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666);
	}
}
