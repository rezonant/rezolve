package com.astronautlabs.mc.rezolve.thunderbolt.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@RegistryId("remote_shell")
@WithBlockEntity(RemoteShellEntity.class)
@WithMenu(RemoteShellMenu.class)
public class RemoteShellBlock extends Machine {
	public RemoteShellBlock() {
		super(Block.Properties.of(Material.METAL));
	}

	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
	}
}
