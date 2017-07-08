package com.astronautlabs.mc.rezolve.common;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

public interface IGuiProvider {
	Container createServerGui(EntityPlayer player, World world, int x, int y, int z);
	GuiContainer createClientGui(EntityPlayer player, World world, int x, int y, int z);
}
