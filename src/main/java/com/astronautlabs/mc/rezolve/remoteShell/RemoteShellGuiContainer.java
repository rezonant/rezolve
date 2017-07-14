package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.unbundler.UnbundlerEntity;

import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;

public class RemoteShellGuiContainer extends GuiContainerBase {

	public RemoteShellGuiContainer(IInventory playerInv, RemoteShellEntity entity) {
		super(new RemoteShellContainer(playerInv, entity), "rezolve:textures/gui/container/remote_shell.png");

		this.playerInv = playerInv;
		this.entity = entity;
	    this.xSize = 255;
	    this.ySize = 212;
	}
	
	private IInventory playerInv;
	private RemoteShellEntity entity;

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
	    //String s = this.entity.getDisplayName().getUnformattedText();
	    //this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
	    //this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

	    int rfBarX = 231;
	    int rfBarY = 20;
	    int rfBarHeight = 88;
	    int rfBarWidth = 14;
	    
	    int usedHeight = rfBarHeight - (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
	    Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + usedHeight, 0xFF000000);
	}
}
