package com.astronautlabs.mc.rezolve.remoteShell;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.RezolvePacketHandler;
import com.astronautlabs.mc.rezolve.bundler.BundlerContainer;
import com.astronautlabs.mc.rezolve.bundler.BundlerEntity;
import com.astronautlabs.mc.rezolve.bundler.BundlerGuiContainer;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RemoteShellBlock extends Machine {

	public RemoteShellBlock() {
		super("block_remote_shell");
		
	}

	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		RemoteShellActivateMessageHandler.register();
		RemoteShellReturnMessageHandler.register();
		RemoteShellRenameMachineMessageHandler.register();
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
