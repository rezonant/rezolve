package com.astronautlabs.mc.rezolve.bundles.machines.bundler;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.*;
import com.astronautlabs.mc.rezolve.core.inventory.ValidatedSlot;
import com.astronautlabs.mc.rezolve.machines.MachineEntity;
import com.astronautlabs.mc.rezolve.machines.MachineGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class BundlerGui extends MachineGui<BundlerEntity> {
	public static ContainerBase<?> createContainerFor(EntityPlayer player, MachineEntity entity) {

		// Input item slots (0-8)
		// Pattern item slots (9-17)
		// Output slots 18-26
		// Player slots

		return BuildableContainer
			.withEntity(entity)
			.slotSize(18)
			.addSlotGrid(0, 20, 45, 3, 3)
			.addValidatedSlotGrid(9, new ValidatedSlot.Validator() {
				@Override
				public boolean validate(ItemStack stack) {
					return stack.getItem() == RezolveMod.BUNDLE_PATTERN_ITEM;
				}
			}, 81, 45, 3, 3)
			.addOutputSlotGrid(18, 165, 45, 3, 3)
			.addPlayerSlots(player.inventory, 47, 131)
			.build();
	}

	@Override
	public void setup() {
		this.guiBackgroundResource = "rezolve:textures/gui/container/bundler_gui.png";
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
