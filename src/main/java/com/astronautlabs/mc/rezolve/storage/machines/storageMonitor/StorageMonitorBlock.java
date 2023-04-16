package com.astronautlabs.mc.rezolve.storage.machines.storageMonitor;

import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.world.level.material.Material;

@RegistryId("storage_monitor")
@WithBlockEntity(StorageMonitorEntity.class)
@WithMenu(StorageMonitorMenu.class)
public class StorageMonitorBlock extends Machine {
	public StorageMonitorBlock() {
		super(Properties.of(Material.METAL));
	}
}
