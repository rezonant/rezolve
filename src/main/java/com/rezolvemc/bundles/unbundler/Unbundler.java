package com.rezolvemc.bundles.unbundler;

import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
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
