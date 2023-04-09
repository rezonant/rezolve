package com.astronautlabs.mc.rezolve.thunderbolt.proxy;

import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.world.level.material.Material;

@RegistryId("proxy")
@WithBlockEntity(ProxyBlockEntity.class)
public class ProxyBlock extends Machine {
    public ProxyBlock() {
        super(Properties.of(Material.METAL));
    }
}
