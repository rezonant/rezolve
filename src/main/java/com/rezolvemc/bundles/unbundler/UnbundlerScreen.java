package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineProgressIndicator;
import com.rezolvemc.common.machines.MachineScreen;

import com.rezolvemc.common.machines.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.ui.layout.AxisAlignment;
import org.torchmc.ui.layout.AxisConstraint;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.PlayerSlotGrid;
import org.torchmc.ui.widgets.SlotGrid;
import org.torchmc.ui.widgets.Spacer;

@ScreenFor(UnbundlerMenu.class)
public class UnbundlerScreen extends MachineScreen<UnbundlerMenu> {

	public UnbundlerScreen(UnbundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 221, 195);
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

		getMainWindow().setResizable(false);

//		addOperationProgressIndicator(leftPos + 103, topPos + 81);
//		addEnergyMeter(leftPos + 226, topPos + 20, 88);
//		addSlotGrid(Component.translatable("screens.rezolve.bundles"), 3, 0, 9);
//		addSlotGrid(Component.translatable("screens.rezolve.items_title"), 4, 9, 16);
	}

	@Override
	public void renderContents(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
		super.renderContents(gfx, mouseX, mouseY, partialTick);

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
		gfx.drawString(font, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666, false);
	}
}
