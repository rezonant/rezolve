package com.astronautlabs.mc.rezolve.bundles.bundleBuilder;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BundleBuilderScreen extends MachineScreen<BundleBuilderMenu> {

	public BundleBuilderScreen(BundleBuilderMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, "rezolve:textures/gui/container/bundle_builder_gui.png", 218, 212);
	}

	@Override
	protected void init() {
		super.init();

		inventoryLabelX = 30;
		inventoryLabelY = 116;
		titleLabelX = 10;
		titleLabelY = 8;
		nameField = new EditBox(font, leftPos + 83, topPos + 41, 88, 13, Component.translatable("screens.rezolve.name"));
		nameField.setMaxLength(23);
		addRenderableWidget(nameField);

		lockPositions = CycleButton
				.booleanBuilder(
						Component.translatable("screens.rezolve.locked"),
						Component.translatable("screens.rezolve.unlocked")
				)
				.displayOnlyValue()
				.create(
						leftPos + 10, topPos + 94, 54, 20,
						Component.translatable("screens.rezolve.positions"),
						(btn, value) -> menu.setLockPositions(value)
				)
		;

		this.addRenderableWidget(lockPositions);
	}

	private EditBox nameField;
	private CycleButton lockPositions;

	@Override
	public void updateStateFromMenu() {
		if ((boolean)lockPositions.getValue() != menu.lockPositions)
			lockPositions.setValue(menu.lockPositions);

		nameField.setValue(menu.patternName);
		super.updateStateFromMenu();
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		var result = super.charTyped(pCodePoint, pModifiers);
		if (result && this.nameField.isFocused())
			menu.setPatternName(this.nameField.getValue());

		return result;
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

		double usedHeight = menu.energyStored / (double)menu.energyCapacity * rfBarHeight;
		colorQuad(pPoseStack, 0, 0, 0, 1, rfBarX, rfBarY, rfBarWidth, rfBarHeight - usedHeight);

		this.colorQuad(pPoseStack, 1, 0, 0, 0, rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight);
		this.colorQuad(pPoseStack, 1, 1, 0, 0, rfBarX, rfBarY + rfBarHeight - usedHeight, rfBarX + rfBarWidth, rfBarY + rfBarHeight);
	}
}
