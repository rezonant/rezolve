package com.rezolvemc.bundles.bundler;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineProgressIndicator;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.machines.Operation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.ui.Window;
import org.torchmc.ui.layout.AxisAlignment;
import org.torchmc.ui.layout.HorizontalLayoutPanel;
import org.torchmc.ui.layout.VerticalLayoutPanel;
import org.torchmc.ui.widgets.PlayerSlotGrid;
import org.torchmc.ui.widgets.SlotGrid;

@ScreenFor(BundlerMenu.class)
public class BundlerScreen extends MachineScreen<BundlerMenu> {

	public BundlerScreen(BundlerMenu menu, Inventory playerInv, Component title) {
		super(menu, playerInv, title, 249, 180);

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
				row.setExpansionFactor(1);

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

			root.addChild(new PlayerSlotGrid(), grid -> {
				grid.setExpansionFactor(1);
			});

		});

		getMainWindow().setResizable(false);
	}

	@Override
	public void renderContents(GuiGraphics gfx, int mouseX, int mouseY, float pPartialTick) {
		super.renderContents(gfx, mouseX, mouseY, pPartialTick);

	    Operation<BundlerEntity> op = this.menu.operation;
	    String statusStr;
	    
	    if (op != null) {
			float progress = menu.progress;
    		statusStr = "Operation: "+(Math.floor(progress * 100))+"%";
	    } else {
	    	statusStr = "Idle.";
	    }

		int titleWidth = font.width(this.title.getString() + " ");
		gfx.drawString(font, statusStr, this.titleLabelX + titleWidth, this.titleLabelY, 0xFF666666, false);
	}
}
