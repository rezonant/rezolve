package com.astronautlabs.mc.rezolve.machines.diskManipulator;

import com.astronautlabs.mc.rezolve.common.BuildableContainer;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public class DiskManipulatorGui extends MachineGui<DiskManipulatorEntity> {
	public static ContainerBase<?> createContainerFor(EntityPlayer player, MachineEntity entity) {
		return BuildableContainer
			.withEntity(entity)
			.slotSize(18)
			.addSlot(0, 4, 107)
			.addPlayerSlots(player.inventory, 47, 131)
			.build();
	}

	@Override
	public void setup() {
		this.guiBackgroundResource = "rezolve:textures/gui/container/disk_manipulator_gui.png";
		this.xSize = 255;
		this.ySize = 212;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		//String s = this.entity.getDisplayName().getUnformattedText();
		//this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
		//this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

		int rfBarX = 245;
		int rfBarY = 3;
		int rfBarHeight = 111;
		int rfBarWidth = 3;

		int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
		Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight - usedHeight, 0xFF000000);
	}
}
