package com.rezolvemc.storage.machines.storageShell;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.ScreenFor;
import com.rezolvemc.storage.view.IStorageViewContainer;
import com.rezolvemc.storage.view.StorageView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.layout.Axis;
import org.torchmc.layout.AxisLayoutPanel;
import org.torchmc.util.Size;
import org.torchmc.widgets.EditBox;
import org.torchmc.widgets.Meter;
import org.torchmc.widgets.SlotGrid;

@ScreenFor(StorageShellMenu.class)
public class StorageShellScreen extends MachineScreen<StorageShellMenu> implements IStorageViewContainer {

	public StorageShellScreen(StorageShellMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, 255, 212);

		enableInventoryLabel = false;
		setMinSize(new Size(170, 200));
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	@Override
	public void setup() {
		super.setup();

		setPanel(new AxisLayoutPanel(Axis.Y), root -> {
			root.setSpace(3);
			root.addChild(new AxisLayoutPanel(Axis.X), panel -> {
				panel.setSpace(3);
				panel.setExpansionFactor(1);
				panel.addChild(
						new Meter(
								Component.translatable("rezolve.screens.usage"),
								Component.literal(""),
								Rezolve.tex("gui/widgets/storage_meter.png")
						)
				);

				panel.addChild(new AxisLayoutPanel(Axis.Y), panel2 -> {
					panel2.setExpansionFactor(1);
					panel2.setSpace(3);

					panel2.addChild(new EditBox(Component.translatable("screens.rezolve.search")), editBox -> {
						searchField = editBox;
					});
					panel2.addChild(new StorageView(), view -> {
						view.setExpansionFactor(1);
						storageView = view;
					});
				});

				panel.addChild(new EnergyMeter());
			});

			root.addChild(new SlotGrid(Component.translatable("screens.resolve.inventory_slot"), 9), grid -> {
				grid.setContents(9, 36);
			});
		});

//		this.searchField = new EditBox(font, this.leftPos + 24, this.topPos + 21, 207, 13, Component.translatable("rezolve.screens.search"));
//		this.searchField.setVisible(true);
//		this.searchField.setValue("");
//		this.searchField.setBordered(true);
//		this.searchField.setFocus(true);
//		this.addRenderableWidget(this.searchField);
//
//		this.storageView = new StorageView(this.leftPos + 23, this.topPos + 36, 210, 85);
//		this.addRenderableWidget(this.storageView);
//
//		addEnergyMeter(leftPos + 234, topPos + 20, 107);
//		addMeter(
//				Component.translatable("rezolve.screens.usage"),
//				Component.literal(""),
//				new ResourceLocation("rezolve", "textures/gui/widgets/storage_meter.png"),
//				leftPos + 5, topPos + 20, 107,
//				menu -> 0.25
//		);

		// -----------------------
		// TODO

//		this.clearCraftingGridBtn = new Button(this.leftPos + 51, this.topPos + 122, 7, 7, Component.literal("×"), (btn) -> {
//			new StorageShellClearCrafterPacket(minecraft.player).sendToServer();
//		});
//
//		this.addRenderableWidget(this.clearCraftingGridBtn);
//
//		addSlotGrid(Component.translatable("rezolve.screens.crafting"), 3, 0, 9, true);
	}

	private EditBox searchField;
	private Button clearCraftingGridBtn;
	private StorageView storageView;

	@Override
	protected void renderContents(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.renderContents(pPoseStack, pMouseX, pMouseY, pPartialTick);
		if (this.storageView != null && this.searchField != null)
			this.storageView.setQuery(this.searchField.getValue());
	}
}
