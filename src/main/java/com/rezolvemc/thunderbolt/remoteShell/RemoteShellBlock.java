package com.rezolvemc.thunderbolt.remoteShell;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.registry.RegistryId;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

@RegistryId("remote_shell")
@WithBlockEntity(RemoteShellEntity.class)
@WithMenu(RemoteShellMenu.class)
public class RemoteShellBlock extends Machine {
	public RemoteShellBlock() {
		super(Block.Properties.of().mapColor(MapColor.METAL));
	}

	@Override
	public void init(Rezolve mod) {
		super.init(mod);
	}
}
