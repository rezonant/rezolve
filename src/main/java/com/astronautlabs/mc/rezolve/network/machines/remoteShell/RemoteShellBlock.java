package com.astronautlabs.mc.rezolve.network.machines.remoteShell;

import com.astronautlabs.mc.rezolve.core.ModBase;
import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.machines.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import com.astronautlabs.mc.rezolve.util.RecipeUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RemoteShellBlock extends Machine {

	public RemoteShellBlock() {
		super("block_remote_shell");
		
	}

	@Override
	public void registerRecipes() {
		ItemStack enderPearlBundle = RezolveMod.BUNDLE_ITEM.withContents(
			1,
			new ItemStack(Items.ENDER_PEARL, 16)
		);

		RecipeUtil.add(
			new ItemStack(this.itemBlock),
			"cDc",
			"iMi",
			"ETE",

			'c', "block_ethernet_cable",
			'D', "item_machine_part|display_panel",
			'i', "item_machine_part|integrated_circuit",
			'M', "block_machine_frame",
			'E', enderPearlBundle,
			'T', "item_machine_part|transcoder"
		);
	}
	
	@Override
	public void init(ModBase mod) {
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
