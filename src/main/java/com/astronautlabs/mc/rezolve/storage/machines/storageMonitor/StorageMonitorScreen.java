package com.astronautlabs.mc.rezolve.storage.machines.storageMonitor;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.astronautlabs.mc.rezolve.storage.view.IStorageViewContainer;
import com.astronautlabs.mc.rezolve.storage.view.StorageView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StorageMonitorScreen extends MachineScreen<StorageMonitorMenu> implements IStorageViewContainer {
	public StorageMonitorScreen(StorageMonitorMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, "rezolve:textures/gui/container/storage_monitor_gui.png", 255, 212);
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	@Override
	public void init() {
		super.init();

		this.searchField = new EditBox(font, leftPos + 7, topPos + 5, 240, 13, Component.translatable("rezolve.screens.search"));
		this.searchField.setVisible(true);
		this.searchField.setValue("");
		this.searchField.setBordered(true);
		this.searchField.setFocus(true);
		//this.searchField.setTextColor(0x000000);
		this.addRenderableWidget(this.searchField);

		this.storageView = new StorageView(this, leftPos + 7, topPos + 20, 240, 90);
		this.addRenderableWidget(this.storageView);

		addEnergyMeter(250, 3, 111);
	}

	private EditBox searchField;
	private StorageView storageView;

	@Override
	protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);

		if (this.storageView != null)
			this.storageView.setQuery(this.searchField.getValue());
	}
}
