package com.astronautlabs.mc.rezolve.storage.machines.storageShell;

import com.astronautlabs.mc.rezolve.common.machines.MachineScreen;
import com.astronautlabs.mc.rezolve.storage.view.IStorageViewContainer;
import com.astronautlabs.mc.rezolve.storage.gui.StorageShellClearCrafterPacket;
import com.astronautlabs.mc.rezolve.storage.view.StorageView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StorageShellScreen extends MachineScreen<StorageShellMenu> implements IStorageViewContainer {

	public StorageShellScreen(StorageShellMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, "rezolve:textures/gui/container/storage_shell_gui.png", 255, 212);
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	public int SEARCH_FIELD = 1;
	public int CLEAR_CRAFTING_GRID_BTN = 2;

	@Override
	protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
		this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
		//this.font.draw(pPoseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
	}

	@Override
	public void init() {
		super.init();

		this.searchField = new EditBox(font, this.leftPos + 24, this.topPos + 21, 207, 13, Component.translatable("rezolve.screens.search"));
		this.searchField.setVisible(true);
		this.searchField.setValue("");
		this.searchField.setBordered(true);
		this.searchField.setFocus(true);


		//this.searchField.setTextColor(0x000000);
		this.addRenderableWidget(this.searchField);

		this.storageView = new StorageView(this, this.leftPos + 23, this.topPos + 36, 210, 85);
		this.addRenderableWidget(this.storageView);

		this.clearCraftingGridBtn = new Button(this.leftPos + 51, this.topPos + 122, 7, 7, Component.literal("×"), (btn) -> {
			new StorageShellClearCrafterPacket(minecraft.player).sendToServer();
		});

		this.addRenderableWidget(this.clearCraftingGridBtn);

		addEnergyMeter(leftPos + 234, topPos + 20, 107);
		addMeter(
				Component.translatable("rezolve.screens.usage"),
				Component.literal(""),
				new ResourceLocation("rezolve", "textures/gui/widgets/storage_meter.png"),
				leftPos + 5, topPos + 20, 107,
				menu -> 0.25
		);

		addSlotGrid(Component.translatable("rezolve.screens.crafting"), 3, 0, 9, false);
	}

	private EditBox searchField;
	private Button clearCraftingGridBtn;
	private StorageView storageView;

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (searchField.isMouseOver(mouseX, mouseY) && mouseButton == 1) {
			this.searchField.setValue("");
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
		if (this.storageView != null)
			this.storageView.setQuery(this.searchField.getValue());
	}
}
