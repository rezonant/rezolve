package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineProgressIndicator;
import com.rezolvemc.common.machines.MachineScreen;

import com.rezolvemc.common.machines.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.layout.AxisAlignment;
import org.torchmc.layout.AxisConstraint;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.widgets.PlayerSlotGrid;
import org.torchmc.widgets.ProgressIndicator;
import org.torchmc.widgets.SlotGrid;
import org.torchmc.widgets.Spacer;

public class UnbundlerScreen extends MachineScreen<UnbundlerMenu> {

	public UnbundlerScreen(UnbundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 255, 212);

		this.inventoryLabelX = 49;
		this.inventoryLabelY = 113;
	}

	@Override
	protected void setup() {
		super.setup();

		setPanel(new VerticalLayoutPanel(), root -> {
			root.addChild(new HorizontalLayoutPanel(), panel -> {
				panel.setAlignment(AxisAlignment.CENTER);
				panel.setJustification(AxisAlignment.CENTER);

				panel.addChild(new Spacer(), spacer -> {
					spacer.setWidthConstraint(AxisConstraint.fixed(5));
				});

				panel.addChild(new SlotGrid(Rezolve.str("bundles"), 3), grid -> {
					grid.setContents(0, 9);
				});

				panel.addChild(new MachineProgressIndicator(), indicator -> {
					indicator.setLeftPadding(0);
					indicator.setRightPadding(-3);
				});

				panel.addChild(new SlotGrid(Rezolve.str("items_title"), 4), grid -> {
					grid.setContents(9, 16);
				});

				panel.addChild(new EnergyMeter());
			});

			root.addChild(new PlayerSlotGrid());
		});

		getMainWindow().resize(221, 212);
		getMainWindow().setResizable(false);

//		addOperationProgressIndicator(leftPos + 103, topPos + 81);
//		addEnergyMeter(leftPos + 226, topPos + 20, 88);
//		addSlotGrid(Component.translatable("screens.rezolve.bundles"), 3, 0, 9);
//		addSlotGrid(Component.translatable("screens.rezolve.items_title"), 4, 9, 16);
	}

	@Override
	public void renderContents(PoseStack pPoseStack, int mouseX, int mouseY, float partialTick) {
		super.renderContents(pPoseStack, mouseX, mouseY, partialTick);

	    // Draw operation details

	    Operation<UnbundlerEntity> op = this.menu.operation;
	    String statusStr;

	    if (op != null) {
    	    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();

    		statusStr = "Operation: "+op.getPercentage()+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		this.font.draw(pPoseStack, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666);
	}
}
