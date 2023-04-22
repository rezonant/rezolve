package com.rezolvemc.thunderbolt.proxy;

import com.rezolvemc.common.blocks.WithBlockEntity;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("proxy")
@WithBlockEntity(ProxyBlockEntity.class)
public class ProxyBlock extends Machine {
    public ProxyBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL));
    }
}
