package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BundleBuilderScreen extends MachineScreen {

	public BundleBuilderScreen(BundleBuilderMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/bundle_builder_gui.png", 218, 212);
	}

	private BundleBuilderEntity entity; // TODO
	private EditBox nameField;
	
	private static final int NAME_FIELD_ID = 1;

	@Override
	protected void init() {
		super.init();
		this.nameField = new EditBox(this.font, this.leftPos + 83, this.topPos + 41, 88, 13, Component.literal("Name"));
		this.nameField.setMaxLength(23);
		//this.nameField.setValue(this.entity.getPatternName() != null ? this.entity.getPatternName() : "");
		this.nameField.changeFocus(true);
		this.addRenderableWidget(this.nameField);
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		entity.setPatternName(this.nameField.getValue());
		return super.charTyped(pCodePoint, pModifiers);
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
		//String s = this.entity.getDisplayName().getUnformattedText();
		//this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
		//this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

		int rfBarX = 191;
		int rfBarY = 17;
		int rfBarHeight = 88;
		int rfBarWidth = 14;

		int usedHeight = 0; // TODO (int)(this.entity.getEnergyStored(Direction.DOWN) / (double)this.entity.getMaxEnergyStored(Direction.DOWN) * rfBarHeight);

		this.colorQuad(pPoseStack, 1, 0, 0, 0, rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight);
		this.colorQuad(pPoseStack, 1, 1, 0, 0, rfBarX, rfBarY + rfBarHeight - usedHeight, rfBarX + rfBarWidth, rfBarY + rfBarHeight);
	}
}
