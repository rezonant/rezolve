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
	protected void init() {
		super.init();

		addOperationProgressIndicator(leftPos + 103, topPos + 81);
		addEnergyMeter(leftPos + 226, topPos + 20, 88);
		addSlotGrid(Component.translatable("screens.rezolve.bundles"), 3, 0, 9);
		addSlotGrid(Component.translatable("screens.rezolve.items_title"), 4, 9, 16);
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float partialTick) {
		super.renderContents(pPoseStack, mouseX, mouseY, partialTick);

	    // Draw operation details

	    Operation<UnbundlerEntity> op = this.menu.operation;
	    String statusStr;

	    if (op != null) {
    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();

    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		this.font.draw(pPoseStack, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666);
	}
}
