package com.rezolvemc.bundles.bundler;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineProgressIndicator;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.machines.Operation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.layout.AxisAlignment;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.widgets.PlayerSlotGrid;
import org.torchmc.widgets.SlotGrid;

public class BundlerScreen extends MachineScreen<BundlerMenu> {

	public BundlerScreen(BundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 255, 212);

		this.inventoryLabelX = 49;
		this.inventoryLabelY = 113;
	}

	@Override
	protected void setup() {
		super.setup();

		setPanel(new VerticalLayoutPanel(), root -> {
			root.addChild(new HorizontalLayoutPanel(), row -> {
				row.setJustification(AxisAlignment.CENTER);
				row.setAlignment(AxisAlignment.CENTER);
				row.setGrowScale(1);

				row.addChild(new SlotGrid(Rezolve.str("input_items"), 3), grid -> {
					grid.setContents(0, 9);
				});

				row.addChild(new SlotGrid(Rezolve.str("patterns"), 3), grid -> {
					grid.setContents(9, 9);
				});

				row.addChild(new MachineProgressIndicator());

				row.addChild(new SlotGrid(Rezolve.str("bundles"), 3), grid -> {
					grid.setContents(18, 9);
				});

				row.addChild(new EnergyMeter());
			});

			root.addChild(new PlayerSlotGrid());
		});

		getMainWindow().resize(249, 212);
		getMainWindow().setResizable(false);
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float pPartialTick) {
		super.renderContents(pPoseStack, mouseX, mouseY, pPartialTick);

	    Operation<BundlerEntity> op = this.menu.operation;
	    String statusStr;
	    
	    if (op != null) {
			float progress = menu.progress;
    		int width = (int)(32 * progress);

    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			textureQuad(pPoseStack, new ResourceLocation("rezolve:textures/gui/container/arrow.png"), 133, 54, width, 32, 0, 0, (float)progress, 1);
    		statusStr = "Operation: "+(Math.floor(progress * 100))+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		this.font.draw(pPoseStack, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666);
	}
}
