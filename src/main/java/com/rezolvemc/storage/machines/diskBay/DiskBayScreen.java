package com.rezolvemc.storage.machines.diskBay;

import com.rezolvemc.common.machines.MachineScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DiskBayScreen extends MachineScreen<DiskBayMenu> {
	public DiskBayScreen(DiskBayMenu menu, Inventory playerInventory, Component pTitle) {
		super(menu, playerInventory, pTitle, 255, 212);

		inventoryLabelX = 47;
		inventoryLabelY = 112;

	}

	@Override
	protected void init() {
		super.init();

		addSlotGrid(Component.translatable("rezolve.screens.disks"), 9, 0, 27, false);
		addEnergyMeter(leftPos + 231, topPos + 20, 88);
		addMeter(
				Component.translatable("rezolve.screens.usage"),
				Component.literal(""),
				new ResourceLocation("rezolve", "textures/gui/widgets/storage_meter.png"),
				leftPos + 10, topPos + 20, 88,
				menu -> 0.25
		);
	}
}
