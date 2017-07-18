package com.astronautlabs.mc.rezolve.securityServer;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SecurityServerBlock extends Machine {

	public SecurityServerBlock() {
		super("block_security_server");
	}

	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		RuleModificationMessageHandler.register();
	}
	
	@Override
	public Container createServerGui(EntityPlayer player, World world, int x, int y, int z) {
		return new SecurityServerContainer(player, (SecurityServerEntity)world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z) {
		return new SecurityServerGuiContainer(player, (SecurityServerEntity)world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public Class<? extends TileEntityBase> getTileEntityClass() {
		return SecurityServerEntity.class;
	}

}
