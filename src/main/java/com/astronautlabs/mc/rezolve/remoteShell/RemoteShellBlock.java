package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.bundler.BundlerContainer;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity;
import com.astronautlabs.mc.rezolve.bundler.BundlerGuiContainer;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RemoteShellBlock extends Machine {

	public RemoteShellBlock() {
		super("block_remote_shell");
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return RemoteShellEntity.class;
	}

	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new RemoteShellContainer(player.inventory, (RemoteShellEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new RemoteShellGuiContainer(player.inventory, (RemoteShellEntity) world.getTileEntity(new BlockPos(x, y, z)));
	}

}
