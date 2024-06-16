package com.rezolvemc.storage.machines.storageMonitor;

import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.material.MapColor;

@RegistryId("storage_monitor")
@WithBlockEntity(StorageMonitorEntity.class)
@WithMenu(StorageMonitorMenu.class)
public class StorageMonitorBlock extends Machine {
	public StorageMonitorBlock() {
		super(Properties.of().mapColor(MapColor.METAL));
	}
}
