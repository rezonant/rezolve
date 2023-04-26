package com.rezolvemc.storage.machines.diskManipulator;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.storage.view.IStorageViewContainer;
import com.rezolvemc.storage.view.StorageView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.widgets.EditBox;
import org.torchmc.widgets.Meter;
import org.torchmc.widgets.SlotGrid;
import org.torchmc.widgets.SlotWidget;

public class DiskManipulatorScreen extends MachineScreen<DiskManipulatorMenu> implements IStorageViewContainer {

	public DiskManipulatorScreen(DiskManipulatorMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, 255, 212);

		inventoryLabelX = 47;
		inventoryLabelY = 113;

		twoToneHeight = 125;
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	protected void setup() {
		super.setup();

		setPanel(new VerticalLayoutPanel(), root -> {
			root.addChild(new HorizontalLayoutPanel(), panel -> {
				panel.setGrowScale(1);

				panel.addChild(new VerticalLayoutPanel(), panel2 -> {
					panel2.addChild(new SlotWidget(Component.empty(), menu.getSlot(0)));
					panel2.addChild(
							new Meter(
									Component.translatable("rezolve.screens.usage"),
									Component.literal(""),
									Rezolve.tex("gui/widgets/storage_meter.png")
							), meter -> {
								meter.setGrowScale(1);
							}
					);
				});

				panel.addChild(new VerticalLayoutPanel(), panel2 -> {
					panel2.setGrowScale(1);
					panel2.setSpace(3);

					panel2.addChild(new org.torchmc.widgets.EditBox(Component.translatable("screens.rezolve.search")), editBox -> {
						searchField = editBox;
					});
					panel2.addChild(new StorageView(), view -> {
						view.setGrowScale(1);
						storageView = view;
					});
				});

				panel.addChild(new EnergyMeter());
			});

			root.addChild(new SlotGrid(Component.translatable("screens.resolve.inventory_slot"), 9), grid -> {
				grid.setContents(1, 36);
			});
		});

		// ----------------
//
//		searchField = new EditBox(font, leftPos + 25, topPos + 18, 207, 13, Component.translatable("rezolve.screens.search"));
//		this.searchField.setVisible(true);
//		this.searchField.setValue("");
//		this.searchField.setBordered(true);
//		this.searchField.setFocus(true);
//		//this.searchField.setTextColor(0x000000);
//
//		this.addRenderableWidget(this.searchField);
//
//		this.storageView = new StorageView(this.leftPos + 24, this.topPos + 33, 209, 75);
//		this.addRenderableWidget(this.storageView);
//
//		addEnergyMeter(leftPos + 235, topPos + 17, 111);
//		addMeter(
//				Component.translatable("rezolve.screens.usage"),
//				Component.literal("%"),
//				new ResourceLocation("rezolve", "textures/gui/widgets/storage_meter.png"),
//				leftPos + 5, topPos + 38, 89,
//				menu -> 0.25
//		);
//
//		addSlot(Component.literal(""), 0);
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
