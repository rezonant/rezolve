package com.astronautlabs.mc.rezolve.machines.bundleBuilder;

import java.io.IOException;

import com.astronautlabs.mc.rezolve.common.GuiContainerBase;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;

public class BundleBuilderGuiContainer extends GuiContainerBase {

	public BundleBuilderGuiContainer(IInventory playerInv, BundleBuilderEntity entity) {
		super(new BundleBuilderContainer(playerInv, entity), "rezolve:textures/gui/container/bundle_builder_gui.png", 218, 212);
		
		this.playerInv = playerInv;
		this.entity = entity;
	}
	
	private IInventory playerInv;
	private BundleBuilderEntity entity;
	private GuiTextField nameField;
	
	private static final int NAME_FIELD_ID = 1;
	
	@Override
	public void initGui() {
		super.initGui();
		this.nameField = new GuiTextField(NAME_FIELD_ID, this.fontRendererObj, this.guiLeft + 83, this.guiTop + 41, 88, 13);
		this.nameField.setMaxStringLength(23);
		this.nameField.setText(this.entity.getPatternName() != null ? this.entity.getPatternName() : "");
		this.nameField.setFocused(true);
		this.addControl(this.nameField);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		entity.setPatternName(this.nameField.getText());
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

		int rfBarX = 191;
		int rfBarY = 17;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight, 0xFF000000);
	    Gui.drawRect(rfBarX, rfBarY + rfBarHeight - usedHeight, rfBarX + rfBarWidth, rfBarY + rfBarHeight, 0xFFFF0000);

	    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}
}
