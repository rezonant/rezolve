package com.astronautlabs.mc.rezolve.thunderbolt.securityServer;

import com.astronautlabs.mc.rezolve.RezolveMod;
import com.astronautlabs.mc.rezolve.common.machines.Machine;
import com.astronautlabs.mc.rezolve.common.blocks.BlockEntityBase;

import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@RegistryId("security_server")
@WithBlockEntity(SecurityServerEntity.class)
@WithMenu(SecurityServerMenu.class)
public class SecurityServer extends Machine {
	private static final Logger LOGGER = LogManager.getLogger();

	public SecurityServer() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}

	@Override
	public void init(RezolveMod mod) {
		super.init(mod);
		//RuleModificationMessageHandler.register();
		this.accessController = new SecurityAccessController();
	}

//	@Override
//	public void registerRecipes() {
//		RezolveMod.addRecipe(
//			new ItemStack(this.itemBlock),
//			"oNo",
//			"cRc",
//			"oDo",
//
//			'o', Blocks.OBSIDIAN,
//			'N', Items.NETHER_STAR,
//			'c', RezolveMod.ETHERNET_CABLE_BLOCK,
//			'R', RezolveMod.REMOTE_SHELL_BLOCK,
//			'D', Items.IRON_DOOR
//		);
//	}
	
	private SecurityAccessController accessController;
	
	public SecurityAccessController getAccessController() {
		return this.accessController;
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
		BlockEntity entity = pLevel.getBlockEntity(pPos);
		if (pPlacer instanceof Player player) {
			SecurityServerEntity securityServerEntity = (SecurityServerEntity)entity;
			securityServerEntity.setRootUser(player);
		}

		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
	}

	@Override
	public Class<? extends BlockEntityBase> getBlockEntityClass() {
		return SecurityServerEntity.class;
	}

}
