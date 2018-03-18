package com.astronautlabs.mc.rezolve.machines.diskBay;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.BuildableContainer;
import com.astronautlabs.mc.rezolve.common.ContainerBase;
import com.astronautlabs.mc.rezolve.common.Operation;
import com.astronautlabs.mc.rezolve.inventory.ValidatedSlot;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class DiskBayGui extends MachineGui<DiskBayEntity> {
	public static ContainerBase<?> createContainerFor(EntityPlayer player, MachineEntity entity) {
		return BuildableContainer
			.withEntity(entity)
			.slotSize(18)
			.addSlotGrid(0, 47, 45, 9, 3)
			.addPlayerSlots(player.inventory, 47, 131)
			.build();
	}


	@Override
	public void setup() {
		this.guiBackgroundResource = "rezolve:textures/gui/container/disk_bay_gui.png";
		this.xSize = 255;
		this.ySize = 212;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		//String s = this.entity.getDisplayName().getUnformattedText();
		//this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
		//this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

		int rfBarX = 231;
		int rfBarY = 20;
		int rfBarHeight = 88;
		int rfBarWidth = 14;

		int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
		Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight - usedHeight, 0xFF000000);
	}
}
