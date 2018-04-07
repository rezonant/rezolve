package com.astronautlabs.mc.rezolve.machines;

import com.astronautlabs.mc.rezolve.common.GuiContainerBase;
import com.astronautlabs.mc.rezolve.common.Operation;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public abstract class MachineGui<T extends MachineEntity> extends GuiContainerBase {

	protected MachineGui() {
		super(null);
	}

	public void initialize(EntityPlayer player, MachineEntity entity) {
		this.player = player;
		this.entity = (T)entity;
		this.inventorySlots = entity.createContainerFor(player);
		this.setup();
	}

	protected EntityPlayer player;
	protected T entity;

	public abstract void setup();

	public EntityPlayer getPlayer() {
		return this.player;
	}

	public IInventory getPlayerInventory() {
		return this.player.inventory;
	}

	public T getEntity() {
		return this.entity;
	}


	@Override
	protected void render(int mouseX, int mouseY) {
		//String s = this.entity.getDisplayName().getUnformattedText();
		//this.fontRendererObj.drawString(s, 88 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);            //#404040
		//this.fontRendererObj.drawString(this.playerInv.getDisplayName().getUnformattedText(), 8, 72, 4210752);      //#404040

		int rfBarX = 231;
		int rfBarY = 20;
		int rfBarHeight = 88;
		int rfBarWidth = 14;

		int usedHeight = (int)(this.entity.getEnergyStored(EnumFacing.DOWN) / (double)this.entity.getMaxEnergyStored(EnumFacing.DOWN) * rfBarHeight);
		Gui.drawRect(rfBarX, rfBarY, rfBarX + rfBarWidth, rfBarY + rfBarHeight - usedHeight, 0xFF000000);

		Operation<? extends TileEntity> op = this.entity.getCurrentOperation();
		String statusStr;

		if (op != null) {
			int width = (int)(32 * op.getPercentage() / (double)100);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mc.getTextureManager().bindTexture(new ResourceLocation("rezolve:textures/gui/container/arrow.png"));
			GlStateManager.enableBlend();
			drawModalRectWithCustomSizedTexture(133, 54, 0, 0, width, 32, 32, 32);

			statusStr = "Operation: "+op.getPercentage()+"%";
		} else {
			statusStr = "Idle.";
		}

		this.fontRendererObj.drawString(statusStr, 7, 102, 0xFF000000);
	}
}
