package com.rezolvemc.thunderbolt.tesseract;

import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("tesseract")
@WithBlockEntity(TesseractEntity.class)
@WithMenu(TesseractMenu.class)
public class Tesseract extends Machine {
    public Tesseract() {
        super(BlockBehaviour.Properties.of(Material.METAL));
    }
}
