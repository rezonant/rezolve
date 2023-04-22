package com.rezolvemc.storage.machines.storageShell;

import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.storage.view.IStorageViewContainer;
import com.rezolvemc.storage.gui.StorageShellClearCrafterPacket;
import com.rezolvemc.storage.view.StorageView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StorageShellScreen extends MachineScreen<StorageShellMenu> implements IStorageViewContainer {

	public StorageShellScreen(StorageShellMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, 255, 212);

		enableInventoryLabel = false;
		twoToneHeight = 125;
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	@Override
	public void setup() {
		super.setup();

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

		addSlotGrid(Component.translatable("rezolve.screens.crafting"), 3, 0, 9, true);
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
