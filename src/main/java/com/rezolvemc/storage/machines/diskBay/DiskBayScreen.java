package com.rezolvemc.storage.machines.diskBay;

import com.rezolvemc.common.gui.EnergyMeter;
import com.rezolvemc.common.machines.MachineScreen;
import com.rezolvemc.common.registry.ScreenFor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.torchmc.layout.HorizontalLayoutPanel;
import org.torchmc.layout.VerticalLayoutPanel;
import org.torchmc.widgets.*;

@ScreenFor(DiskBayMenu.class)
public class DiskBayScreen extends MachineScreen<DiskBayMenu> {
	public DiskBayScreen(DiskBayMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, 255, 212);

		inventoryLabelX = 47;
		inventoryLabelY = 112;

	}

	@Override
	protected void setup() {
		super.setup();

		setPanel(new VerticalLayoutPanel(), root -> {
			root.addChild(new HorizontalLayoutPanel(), top -> {
				top.setGrowScale(1);

				top.addChild(new Spacer());
				top.addChild(new Meter(
						Component.translatable("rezolve.screens.usage"),
						Component.literal(""), new ResourceLocation("rezolve", "textures/gui/widgets/storage_meter.png")
				), meter -> {

				});

				top.addChild(new SlotGrid(Component.translatable("rezolve.screens.disks"), 9), grid -> {
					grid.setContents(0, 27);
				});

				top.addChild(new EnergyMeter());
				top.addChild(new Spacer());
			});

			root.addChild(new PlayerSlotGrid());
		});

//		addSlotGrid(Component.translatable("rezolve.screens.disks"), 9, 0, 27, false);
//		addEnergyMeter(leftPos + 231, topPos + 20, 88);
//		addMeter(
//				Component.translatable("rezolve.screens.usage"),
//				Component.literal(""),
//				new ResourceLocation("rezolve", "textures/gui/widgets/storage_meter.png"),
//				leftPos + 10, topPos + 20, 88,
//				menu -> 0.25
//		);
	}
}
