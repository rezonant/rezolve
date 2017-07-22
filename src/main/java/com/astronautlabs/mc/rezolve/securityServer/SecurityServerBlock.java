package com.astronautlabs.mc.rezolve.securityServer;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.Machine;
import com.astronautlabs.mc.rezolve.common.TileEntityBase;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
		this.accessController = new SecurityAccessController();
	}

	@Override
	public void registerRecipes() {
		RezolveMod.addRecipe(
			new ItemStack(this.itemBlock), 
			"oNo",
			"cRc",
			"oDo", 
			
			'o', Blocks.OBSIDIAN,
			'N', Items.NETHER_STAR,
			'c', RezolveMod.ETHERNET_CABLE_BLOCK,
			'R', RezolveMod.REMOTE_SHELL_BLOCK,
			'D', Items.IRON_DOOR
		);
	}
	
	private SecurityAccessController accessController;
	
	public SecurityAccessController getAccessController() {
		return this.accessController;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		TileEntity entity = worldIn.getTileEntity(pos);
		if (entity != null) {
			SecurityServerEntity securityServerEntity = (SecurityServerEntity)entity;
			securityServerEntity.setRootUser(placer);
		}
		
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
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
