package com.astronautlabs.mc.rezolve.storage.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.astronautlabs.mc.rezolve.storage.view.IStorageViewContainer;
import com.astronautlabs.mc.rezolve.storage.view.StorageView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DiskManipulatorScreen extends MachineScreen<DiskManipulatorMenu> implements IStorageViewContainer {

	public DiskManipulatorScreen(DiskManipulatorMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, "rezolve:textures/gui/container/disk_manipulator_gui.png", 255, 212);

		inventoryLabelX = 47;
		inventoryLabelY = 116;
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	@Override
	protected void init() {
		super.init();

		searchField = new EditBox(font, leftPos + 23, topPos + 5, 216, 13, Component.translatable("rezolve.screens.search"));
		this.searchField.setVisible(true);
		this.searchField.setValue("");
		this.searchField.setBordered(true);
		this.searchField.setFocus(true);
		//this.searchField.setTextColor(0x000000);

		this.addRenderableWidget(this.searchField);

		this.storageView = new StorageView(this, this.leftPos + 24, this.topPos + 20, 209, 90);
		this.addRenderableWidget(this.storageView);

		addEnergyMeter(leftPos + 235, topPos + 3, 111);
	}

	private EditBox searchField;
	private StorageView storageView;

	@Override
	protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);

		if (this.storageView != null) {
			this.storageView.setQuery(this.searchField.getValue());

			var diskInSlot = menu.getSlot(0).getItem();
			if (diskInSlot == null)
				this.storageView.setNoConnectionMessage("Please insert a disk");
			else
				this.storageView.setNoConnectionMessage("Please wait...");
		}
	}
}
