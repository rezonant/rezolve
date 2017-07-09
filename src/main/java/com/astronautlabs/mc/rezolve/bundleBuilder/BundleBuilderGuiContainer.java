package com.astronautlabs.mc.rezolve.bundleBuilder;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class BundleBuilderGuiContainer extends GuiContainer {

	public BundleBuilderGuiContainer(IInventory playerInv, BundleBuilderEntity entity) {
		super(new BundleBuilderContainer(playerInv, entity));
		
		this.playerInv = playerInv;
		this.entity = entity;
	    this.xSize = 218;
	    this.ySize = 212;
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
		this.nameField.setText("");
		this.nameField.setFocused(true);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
  
		
		if (this.nameField.isFocused()) {

			if (keyCode != Keyboard.KEY_E)
				super.keyTyped(typedChar, keyCode);
			
			this.nameField.textboxKeyTyped(typedChar, keyCode);
			
			// TODO: send to server
			
			entity.setPatternName(this.nameField.getText());
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.nameField.updateCursorCounter();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		this.nameField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	    this.mc.getTextureManager().bindTexture(new ResourceLocation("rezolve:textures/gui/container/bundle_builder_gui.png"));
	    this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

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

	}
}
