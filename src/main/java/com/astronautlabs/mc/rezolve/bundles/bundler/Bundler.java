package com.astronautlabs.mc.rezolve.bundles.bundler;

import com.astronautlabs.mc.rezolve.common.machines.Machine;

import com.astronautlabs.mc.rezolve.common.registry.RegistryId;
import com.astronautlabs.mc.rezolve.common.blocks.WithBlockEntity;
import com.astronautlabs.mc.rezolve.common.gui.WithMenu;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

@RegistryId("bundler")
@WithBlockEntity(BundlerEntity.class)
@WithMenu(BundlerMenu.class)
public class Bundler extends Machine {
	public Bundler() {
		super(BlockBehaviour.Properties.of(Material.METAL));
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

		return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
	}

	//	@Override
//	public void registerRecipes() {
//
//		if (Item.REGISTRY.getObject(new ResourceLocation("enderio:itemAlloy")) != null) {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"VSV",
//				"CMC",
//				"VFV",
//
//				'V', "item|enderio:itemAlloy|2",
//				'S', "block|minecraft:sticky_piston",
//				'C', "block|minecraft:chest",
//				'M', "item|enderio:itemMachinePart|0",
//				'F', "item|enderio:itemBasicFilterUpgrade"
//			);
//
//		} else {
//			RezolveMod.addRecipe(
//				new ItemStack(this.itemBlock),
//				"IMI",
//				"CSC",
//				"IHI",
//
//				'I', "item|minecraft:iron_block",
//				'M', "item|minecraft:minecart",
//				'C', "item|minecraft:chest",
//				'S', "item|minecraft:sticky_piston",
//				'H', "item|minecraft:hopper"
//			);
//		}
//	}
}
