package com.astronautlabs.mc.rezolve.bundles.unbundler;

import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("unbundler")
@WithBlockEntity(UnbundlerEntity.class)
@WithMenu(UnbundlerMenu.class)
public class Unbundler extends Machine {
	public Unbundler() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}
}
