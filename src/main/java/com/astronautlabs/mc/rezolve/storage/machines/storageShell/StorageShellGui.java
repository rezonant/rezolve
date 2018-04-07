package com.astronautlabs.mc.rezolve.storage.machines.storageShell;

import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.common.BuildableContainer;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.storage.gui.IStorageViewContainer;
import com.astronautlabs.mc.rezolve.storage.gui.StorageShellClearCrafterMessage;
import com.astronautlabs.mc.rezolve.storage.gui.StorageView;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class StorageShellGui extends MachineGui<StorageShellEntity> implements IStorageViewContainer {
	public static ContainerBase<?> createContainerFor(EntityPlayer player, MachineEntity entity) {
		StorageShellEntity storageShellEntity = (StorageShellEntity)entity;

		return BuildableContainer
			.withEntity(entity)
			.slotSize(18)
			.addSlotGrid(0, 7, 131, 3, 3)
			//.addOutputSlot(9, 64, 149)
			.addSlot(new SlotCrafting(player, storageShellEntity.getCraftMatrix(), storageShellEntity.getCraftResult(), 0, 64, 149))
			.addPlayerSlots(player.inventory, 88, 131)
			.build();
	}

	@Override
	public StorageView getStorageView() {
		return storageView;
	}

	public int SEARCH_FIELD = 1;
	public int CLEAR_CRAFTING_GRID_BTN = 2;

	@Override
	public void setup() {
		this.guiBackgroundResource = "rezolve:textures/gui/container/storage_shell_gui.png";
		this.xSize = 255;
		this.ySize = 212;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.searchField = new GuiTextField(SEARCH_FIELD, this.fontRendererObj, this.guiLeft + 6, this.guiTop + 5, 243, 13);
		this.searchField.setVisible(true);
		this.searchField.setText("");
		this.searchField.setEnableBackgroundDrawing(true);
		this.searchField.setFocused(true);
		//this.searchField.setTextColor(0x000000);
		this.addControl(this.searchField);

		this.storageView = new StorageView(this, this.guiLeft + 7, this.guiTop + 20, 240, 90);
		this.addControl(this.storageView);

		this.clearCraftingGridBtn = new GuiButton(CLEAR_CRAFTING_GRID_BTN, this.guiLeft + 51, this.guiTop + 122, 7, 7, "×");

		this.addControl(this.clearCraftingGridBtn);
	}

	private GuiTextField searchField;
	private GuiButton clearCraftingGridBtn;
	private StorageView storageView;

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		boolean onSearch = mouseX >= this.searchField.xPosition && mouseX < this.searchField.xPosition + this.searchField.width && mouseY >= this.searchField.yPosition && mouseY < this.searchField.yPosition + this.searchField.height;

		if (onSearch && mouseButton == 1) {
			this.searchField.setText("");
		}

		if (this.clearCraftingGridBtn.mousePressed(this.mc, mouseX, mouseY)) {
			RezolvePacketHandler.INSTANCE.sendToServer(new StorageShellClearCrafterMessage(this.mc.thePlayer));
		}
	}

	@Override
	protected void render(int mouseX, int mouseY) {

		//String s = this.entity.getDisplayName().getUnformattedText();
		//this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
		//this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

		int rfBarX = 250;
		int rfBarY = 4;
		int rfBarHeight = 107;
		int rfBarWidth = 3;

		int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
		Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight - usedHeight, 0xFF000000);



		this.storageView.setQuery(this.searchField.getText());

	}
}
