package com.rezolvemc.thunderbolt.securityServer;

import com.rezolvemc.Rezolve;
import com.rezolvemc.common.machines.Machine;
import com.rezolvemc.common.blocks.BlockEntityBase;

import com.rezolvemc.common.registry.RegistryId;
import com.rezolvemc.common.registry.WithBlockEntity;
import com.rezolvemc.common.registry.WithMenu;
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
	public void init(Rezolve mod) {
		super.init(mod);
		//RuleModificationMessageHandler.register();
		this.accessController = new SecurityAccessController();
	}
	
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
