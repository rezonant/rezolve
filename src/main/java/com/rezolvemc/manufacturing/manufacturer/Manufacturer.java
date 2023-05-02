package com.rezolvemc.manufacturing.manufacturer;

import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import net.minecraft.world.level.material.Material;

@RegistryId("manufacturer")
@WithBlockEntity(ManufacturerEntity.class)
@WithMenu(ManufacturerMenu.class)
public class Manufacturer extends Machine {
    public Manufacturer() {
        super(Properties.of(Material.METAL));
    }
}
